## VP App

## Описание

VP App — это Android-приложение, которое собирает геоданные устройства, отправляет их на сервер через WebSocket и предоставляет удобный пользовательский интерфейс с использованием Jetpack Compose. Благодаря гибкой архитектуре, приложение использует DSL (Domain Specific Language) для декларативной настройки навигации и управления потоками данных.

## Функциональные возможности
Получение текущей геолокации устройства (широта, долгота).
Периодическая отправка данных о местоположении через WebSocket-соединение.
Пользовательский интерфейс с возможностью переключения между экраном данных и картой.
Настройка интерфейса и логики приложения с использованием DSL, что упрощает поддержку и расширение функционала.

## Структура проекта

**Файлы и их роль**

*MainActivity.kt*:

Главный класс приложения, управляющий жизненным циклом приложения.
Инициализирует основные компоненты: получение геоданных, WebSocket и данные сотовых вышек.
Использует DSL для настройки геолокационных функций и пользовательского интерфейса.
GeoLocationSession.kt:

Файл с реализацией DSL для декларативного управления геоданными и интерфейсом.
Включает:

*GeolocationSessionScope* — основной контекст DSL, предоставляющий API для настройки логики.
*NavigationScope* — вложенный контекст для настройки навигации и экранов.
*BottomBarScope* — вспомогательный контекст для добавления экранов в нижнюю панель навигации.

*LocationAct.kt*:

Класс для получения данных геолокации с использованием Google Location Services.
Предоставляет текущую широту и долготу, а также методы для управления обновлениями местоположения.
*WebSocketAct.kt*:

Класс для управления WebSocket-соединением с использованием OkHttp.
Реализует отправку данных на сервер и настройку периодической отправки.
*Interface.kt*:

Компонент пользовательского интерфейса, отображающий текущие геоданные и кнопку для их отправки вручную.
*MapViewComposable.kt*:

UI-компонент для отображения текущего местоположения на карте.
DSL: Domain Specific Language
Приложение использует DSL, чтобы упростить и улучшить управление логикой работы с геоданными, сетевыми запросами и пользовательским интерфейсом. Это делает код более читаемым, декларативным и легко расширяемым.

## Как работает DSL

**Главная точка входа:**

geolocationSession(
    locationAct = locationAct,
    webSocketAct = webSocketAct,
    cellInfoAct = cellInfoAct
) {
    ...
}
Функция geolocationSession создаёт контекст GeolocationSessionScope для выполнения настроек.

**Настройка геоданных и периодической отправки:**

startLocationUpdates()
periodicallySendData(intervalMillis = 5000) {
    sendToWebSocket()
}

Простые декларативные вызовы для запуска получения геоданных и настройки отправки.

**Настройка пользовательского интерфейса:**

navigationUI {
    bottomBar {
        screen("location", Icons.Filled.LocationOn) {
            Interface(
                locationAct = locationAct,
                webSocketAct = webSocketAct,
                lifecycleOwner = this@MainActivity
            )
        }
        screen("map", Icons.Filled.Home) {
            MapScreen(locationAct = locationAct, context = this@MainActivity)
        }
    }
}

Через вложенный контекст NavigationScope настраиваются экраны и нижняя панель навигации.

## Как это работает

**Инициализация компонентов:**

При запуске приложения MainActivity инициализирует:
*LocationAct* для работы с геоданными.
*WebSocketAct* для сетевого взаимодействия.
*CellInfoAct* для получения информации о сотовых вышках.

**Работа с геоданными:**

*LocationAct* обновляет координаты (широту и долготу) устройства.
*WebSocketAct* отправляет координаты на сервер.
UI с Jetpack Compose:

Нижняя панель позволяет переключаться между двумя экранами:
Экран с текущими геоданными и возможностью их отправки.
Карта с отображением местоположения.

## Настройка через DSL:

*Все ключевые функции и интерфейс приложения описаны декларативно, что упрощает добавление нового функционала и поддержку существующего.*

**Пример использования DSL**

geolocationSession(
    locationAct = locationAct,
    webSocketAct = webSocketAct,
    cellInfoAct = cellInfoAct
) {
    startLocationUpdates() // Запуск получения геоданных
    periodicallySendData(5000) { // Периодическая отправка данных
        sendToWebSocket()
    }
    navigationUI { // Настройка UI
        bottomBar {
            screen("location", Icons.Filled.LocationOn) {
                Interface(locationAct, webSocketAct, lifecycleOwner)
            }
            screen("map", Icons.Filled.Home) {
                MapScreen(locationAct, context)
            }
        }
    }
}.RenderUI() // Отображение UI

## Возможные будущие улучшения

**1. Добавление исторических данных о местоположении.**
**2. Поддержка пользовательских серверов через конфигурацию.**
**3. Более точные метрики качества соединения (например, RSRP/RSRQ).**
**4. Поддержка нескольких тем для интерфейса (светлая/тёмная).**

## Требования

Android API 21+

## Подключённые библиотеки:

Jetpack Compose
OkHttp
Google Location Services
OSMDroid

## Установка

**1. Склонируйте репозиторий:**
git clone https://github.com/hgwstr/VP
**2. Откройте проект в Android Studio.**
**3. Соберите и установите приложение на устройство.**
