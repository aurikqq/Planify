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
//const val TEXT_WIDGET_TEXT = "text_widget_text"
//const val TEXT_WIDGET_TITLE = "text_widget_title"
//const val TEXT_WIDGET_DATA = "text_widget_data"
const val PLANS_PREFIX = "plans_prefix"
const val IS_PREFIX_HINT_SHOWN = "is_prefix_hint_shown"
const val CHANGELOG = "- Разные оптимизации, теперь должно работать побыстрее\n" +
        "- Исправления багов:\n" +
        "        * Боковое меню больше не открывается (в настройках)" +
        "        * Уведомления о сбросе не появляются без причины ночью после перезагрузки (сколько с ним проблем...)" +
        "        * Индикатор в верхней  панели главного экрана теперь окрашен как должно быть (надеюсь)"
