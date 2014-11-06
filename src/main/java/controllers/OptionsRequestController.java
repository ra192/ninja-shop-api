package controllers;

import filters.CorsFilter;
import ninja.FilterWith;
import ninja.Result;
import ninja.Results;

/**
 * Created by yakov_000 on 06.11.2014.
 */
@FilterWith(CorsFilter.class)
public class OptionsRequestController {

    public Result options() {
        return Results.json().render("options").addHeader("Access-Control-Allow-Headers","Content-Type, x-xsrf-token");
    }
}
