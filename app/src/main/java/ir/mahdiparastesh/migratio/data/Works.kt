package ir.mahdiparastesh.migratio.data

enum class Works {
    GET_ALL, GET_ONE, INSERT_ALL, DELETE_ALL, CLEAR_AND_INSERT_ALL,

    // Purposes
    NONE, CHECK, DOWNLOAD, SAVE_MY_COUNTRIES, EXIT_ON_SAVED,
    NOTIFY_ON_SAVED, BREAK_CENSOR, REPAIR, IMPORT
}
