package npk.pspValidator.web.backend.job_execution_service.jobs;

public class ExecutionJob extends Job {

    public ExecutionJob(String validationId) {
        super(validationId);
    }

    @Override
    public void run() {
        new Thread(() -> {
            updateValidationState("EXECUTING");
            //TODO: actually implement
            updateValidationState("FINISHED");
        }).start();
    }

}
