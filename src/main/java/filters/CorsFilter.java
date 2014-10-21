package filters;

import ninja.Context;
import ninja.Filter;
import ninja.FilterChain;
import ninja.Result;

/**
 * Created by yakov_000 on 21.10.2014.
 */
public class CorsFilter implements Filter {
    @Override
    public Result filter(FilterChain filterChain, Context context) {
        return filterChain.next(context).addHeader("Access-Control-Allow-Origin","*");
    }
}
