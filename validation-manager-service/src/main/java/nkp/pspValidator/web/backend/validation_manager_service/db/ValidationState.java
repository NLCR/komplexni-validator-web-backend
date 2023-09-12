package nkp.pspValidator.web.backend.validation_manager_service.db;

public enum ValidationState {
    READY_FOR_EXTRACTION, // po nahrání, ceka na dalsi zpracování (JobManagerem)
    TO_BE_EXTRACTED, // JobManager oznacil, ze se ma spustit extrakce, nasledne poveri job
    EXTRACTING, // proces (spusteny JobManagerem) rozbaluje, nasledne detekuje parcelId a preda ValidationMgr event o zmene stavu (na ERROR, nebo READY, potom vcetne velikosti baliku a parcelId)

    READY_FOR_EXECUTION, //pripraveno na validaci, jakmile JobManager vyhodnoti, ze kvoty dovoli (bezici validace, podle velikosti a priority), zmeni stav na starting
    TO_BE_EXECUTED, // JobManager oznacil, ze se ma spustit validace, nasledne poveri job
    EXECUTING, //validace probiha
    FINISHED, //validace dokoncena, planovac muze mazat

    ERROR, //incorrect data, neco spadlo atd.
    CANCELED, //zruseno adminem

    TO_BE_ARCHIVED, //ValidationMgr oznacil, ze se muzou smazat data baliku, poveril job
    ARCHIVING, //probiha mazani dat baliku
    ARCHIVED, //data baliku smazana, logy zustavaji

    T0_BE_DELETED, //ValidationMgr oznacil, ze se muze smazat kompletni zaznam validace (logy, db?)
    DELETING, //maze se zaznam validace
    DELETED //uchovany jen zaznamy v db, pracovni adresar vcetne logu smazan

}
