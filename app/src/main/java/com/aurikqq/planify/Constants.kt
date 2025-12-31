package com.aurikqq.planify

val mainScreenTabs = arrayOf(PLANS_SCREEN, NOTES_SCREEN)

const val PREFERENCES_NAME = "com.aurikqq.planify.AppPreferences"
const val KEY_PLANS = "user_plans"
const val KEY_DAILY_PLANS_HISTORY = "daily_plans_history"
const val KEY_NOTES_LIST = "notes_list"
const val KEY_DAILY_PLANS_LIST = "daily_plans_list"

const val KEY_IS_UPDATE_CHANGELOG_SHOWN = "is_update_changelog_shown"

const val KEY_TEMP_PLANS = "temp_plans"
const val KEY_TEMP_NOTE_TITLE = "temp_note_title"
const val KEY_TEMP_NOTE_TEXT = "temp_note_text"

const val KEY_HAVE_PLANS = "does_user_have_plans"
const val KEY_IS_FIRST_LAUNCH = "is_first_launch"

const val MAIN_SCREEN = "main"
const val PLANS_SCREEN = "plans"
const val NOTES_SCREEN = "notes"
const val HISTORY_SCREEN = "history"
const val SETTINGS_SCREEN = "settings"

const val PLANS_NOTIFICATIONS_ENABLED = "plans_notifications_enabled"
const val RESET_NOTIFICATIONS_ENABLED = "notes_notifications_enabled"
const val PLANS_NOTIFICATIONS_COOLDOWN = "plans_notifications_cooldown"
const val IS_DARK_THEME_ON = "is_dark_theme_on"
const val USER_EMAIL = "user_email"
const val CHANGELOG = "В результате крайне димломативных, культурных и вежливых переговоров из Planify были депортированы следующие баги:\n" +
        "• Тот, который скривил новогоднюю шапку в списке дней (весь вайб испортил!!)\n" +
        "• Зависание снегопада после закрытия-открытия приложения (ещё один!)\n" +
        "• Долгая загрузка истории... \uD83D\uDE34\n" +
        "• Слишком большой отступ в настройках, когда над нижней панелью была прозрачная полоса\n" +
        "• Уведомления о сбросе планов иногда писались так, будто на завтра что-то запланировано\n\n" +
        "Также поменялась иконка (она подстраивается под цвета с обоев!), " +
        "теперь капитализация при вводе соотвествует настройкам клавиатуры (ставится большая буква в начале предложения, если ты отключил это), " +
        "раскрытие записей синхронизируется (то есть они не будут закрываться просто так), " +
        "поправлены анимации сдвигания панелей в записях/истории, " +
        "и, может, что-то ещё исправлено. Убраны кнопка \"Проверить обновления\" " +
        "и синхронизация для устройств с Android ниже 14-го - она всё равно пока не поддерживается там.\n\n" +
        "Осталось сделать ещё пару вещей, что хотел успеть до Нового года - поправить кое-что, кое-где улучшить... но хочу выпустить то, что есть, до конца года. " +
        "Удачи в новом году - желаю, чтобы все планы исполнялись, но дни не превратились в сплошные списки дел! \uD83C\uDF87❄\uFE0F\n\n" +
        "С надеждой на лучшее, А.\n\n"
