package demo.callback;

import org.jetbrains.annotations.NotNull;

public class RegisterResponse {

    protected final @NotNull String name;
    protected final boolean success;

    public RegisterResponse(@NotNull String name,
                            boolean success) {
        this.name = name;
        this.success = success;
    }

    public @NotNull String getName() {
        return name;
    }

    public boolean isSuccess() {
        return success;
    }

}
