# Ovrex Proxy

<div align="center">
  <h3>Лёгкий Minecraft-прокси на Java 21</h3>
  <p>Полная документация: <a href="https://docs.alexec0de.pro">docs.alexec0de.pro</a></p>
</div>

## О проекте

**Ovrex** — модульный прокси для Minecraft-сетей с поддержкой:
- подключений игроков через Netty;
- переключения между backend-серверами;
- плагинов и событийной шины;
- Tower-канала для регистрации серверов.

## Модули

- `api` — публичные интерфейсы.
- `network` — сетевой протокол и обработчики пакетов.
- `tower` — регистрация backend-серверов.
- `plugin` — загрузка и управление плагинами.
- `core` — запуск прокси и связывание всех компонентов.

## Быстрый старт

Требования:
- JDK 21

Сборка:

```bash
bash ./gradlew build
```

Запуск:

```bash
bash ./gradlew :core:run
```

или

```bash
java -jar core/build/libs/core-<version>.jar
```

## Документация

Подробные гайды, конфигурация, протоколы и примеры:

➡️ **https://docs.alexec0de.pro**

## Лицензия

См. файл `LICENSE`.
