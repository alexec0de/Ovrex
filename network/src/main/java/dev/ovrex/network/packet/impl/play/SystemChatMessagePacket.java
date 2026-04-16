package dev.ovrex.network.packet.impl.play;

import dev.ovrex.network.packet.Packet;
import dev.ovrex.network.packet.PacketBuffer;
import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Getter
@NoArgsConstructor
public class SystemChatMessagePacket implements Packet {
    private String message;
    private boolean overlay;
    private int protocolId;
    private int position;

    public SystemChatMessagePacket(String message, boolean overlay, int protocolVersion) {
        this.message = message;
        this.overlay = overlay;
        this.protocolId = resolvePacketId(protocolVersion);
    }

    private static int resolvePacketId(int protocolVersion) {
        return 0x73;
    }

    @Override
    public int getId() {
        return protocolId;
    }

    @Override
    public void read(PacketBuffer buffer) {

    }

    @Override
    public void write(PacketBuffer buffer) {
        log.debug(message);
        writeNbtTextComponent(buffer, message);
        buffer.writeBoolean(overlay);
    }

    private void writeNbtTextComponent(PacketBuffer buf, String rawMessage) {
        List<TextSegment> segments = parseSegments(rawMessage);

        if (segments.isEmpty()) {
            segments = new ArrayList<>();
            segments.add(new TextSegment("", null));
        }

        TextSegment root = segments.get(0);
        List<TextSegment> extra = segments.subList(1, segments.size());

        // TAG_Compound root (id=10, без имени для root-тега)
        buf.writeByte(10);
        writeCompoundBody(buf, root, extra);
    }

    private void writeCompoundBody(PacketBuffer buf, TextSegment segment, List<TextSegment> extra) {
        // TAG_String "text"
        buf.writeByte(8);
        writeNbtString(buf, "text");
        writeNbtString(buf, segment.text);

        // TAG_String "color" (если есть)
        if (segment.color != null) {
            buf.writeByte(8);
            writeNbtString(buf, "color");
            writeNbtString(buf, segment.color);
        }

        // TAG_List "extra" (если есть)
        if (!extra.isEmpty()) {
            buf.writeByte(9);   // TAG_List
            writeNbtString(buf, "extra");
            buf.writeByte(10);  // элементы типа TAG_Compound
            buf.writeInt(extra.size());

            for (TextSegment seg : extra) {
                // Элементы TAG_List не имеют id/name-заголовка,
                // просто пишем содержимое compound
                writeCompoundBody(buf, seg, List.of());
            }
        }

        // TAG_End — закрываем compound
        buf.writeByte(0);
    }

    // ─────────────────────────────────────────────────────────────
    // NBT String helper
    // ─────────────────────────────────────────────────────────────

    private void writeNbtString(PacketBuffer buf, String str) {
        byte[] bytes = str.getBytes(StandardCharsets.UTF_8);
        buf.writeShort(bytes.length);
        buf.writeBytes(bytes);
    }

    // ─────────────────────────────────────────────────────────────
    // Парсер §-кодов → сегменты с цветами
    // ─────────────────────────────────────────────────────────────

    private List<TextSegment> parseSegments(String raw) {
        List<TextSegment> result = new ArrayList<>();
        if (raw == null || raw.isEmpty()) return result;

        String currentColor = null;
        StringBuilder currentText = new StringBuilder();

        int i = 0;
        while (i < raw.length()) {
            char c = raw.charAt(i);

            if (c == '§' && i + 1 < raw.length()) {
                char code = Character.toLowerCase(raw.charAt(i + 1));
                String resolved = resolveColor(code);

                if (resolved != null) {
                    // Сохраняем текущий сегмент (если есть текст)
                    if (currentText.length() > 0) {
                        result.add(new TextSegment(currentText.toString(), currentColor));
                        currentText.setLength(0);
                    }
                    currentColor = resolved;
                }
                // Форматирование (bold/italic/etc.) — пропускаем, не меняем цвет
                i += 2;
            } else {
                currentText.append(c);
                i++;
            }
        }

        // Последний сегмент
        if (currentText.length() > 0) {
            result.add(new TextSegment(currentText.toString(), currentColor));
        }

        return result;
    }

    /**
     * Соответствие §-кода → Minecraft named color.
     */
    private String resolveColor(char code) {
        return switch (code) {
            case '0' -> "black";
            case '1' -> "dark_blue";
            case '2' -> "dark_green";
            case '3' -> "dark_aqua";
            case '4' -> "dark_red";
            case '5' -> "dark_purple";
            case '6' -> "gold";
            case '7' -> "gray";
            case '8' -> "dark_gray";
            case '9' -> "blue";
            case 'a' -> "green";
            case 'b' -> "aqua";
            case 'c' -> "red";
            case 'd' -> "light_purple";
            case 'e' -> "yellow";
            case 'f' -> "white";
            default  -> null; // форматирование (k,l,m,n,o,r) — игнорируем
        };
    }

    // ─────────────────────────────────────────────────────────────
    // Модель сегмента
    // ─────────────────────────────────────────────────────────────

    private record TextSegment(String text, String color) {}

}
