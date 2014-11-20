package filters;

import annotations.AllowedRoles;
import com.google.inject.Inject;
import dao.UserDao;
import model.User;
import ninja.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by yakov_000 on 19.11.2014.
 */
public class SecurityFilter implements Filter {

    Logger logger = LoggerFactory.getLogger(SecurityFilter.class);

    @Inject
    UserDao userDao;

    @Override
    public Result filter(FilterChain filterChain, Context context) {

        final String accessToken = context.getHeader("accessToken");

        if(accessToken==null) {
            return Results.forbidden().json().render("error","accessToken header is not specified");
        }

        final User user = userDao.getByAccessToken(accessToken);
        if(user==null) {
            return Results.forbidden().json().render("error","invalid accessToken");
        }

        final Route route = context.getRoute();
        AllowedRoles allowedRoles = route.getControllerMethod().getAnnotation(AllowedRoles.class);
        if (allowedRoles==null) {
            allowedRoles=route.getControllerClass().getAnnotation(AllowedRoles.class);
            if(allowedRoles==null) {
                logger.error("AllowedRoles annotation not specified for {0} or {1}",route.getControllerMethod(),route.getControllerClass());
                return Results.forbidden().json().render("error","internal error");
            }
        }

        boolean isAllowed=false;
        for (int i=0;i<allowedRoles.roles().length;i++){
            if(user.getRoles().contains(allowedRoles.roles()[i])) {
                isAllowed=true;
                break;
            }
        }

        if(!isAllowed) {
            logger.error("Unauthorized access. User: {0}, path: {1}",user.getEmail(),route.getUri());
            return Results.forbidden().json().render("error","unauthorized access");
        }

        return filterChain.next(context);
    }
}
