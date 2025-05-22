package demo.callback;

import org.jetbrains.annotations.NotNull;

public class RegisterRequest {

    protected final @NotNull String name;

    public RegisterRequest(@NotNull String name) {
        this.name = name;
    }

    public @NotNull String getName() {
        return name;
    }


}
