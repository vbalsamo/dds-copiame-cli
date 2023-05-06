package ar.utn.dds.copiame;

import io.javalin.http.Context;
import io.javalin.http.Handler;
import org.jetbrains.annotations.NotNull;

public class AnalisisListController implements Handler {
    private AnalsisRepository repo;

    public AnalisisListController(AnalsisRepository repo) {
        super();
        this.repo = repo;
    }

    @Override
    public void handle(@NotNull Context ctx) throws Exception {
        ctx.json(repo.all());
    }
}