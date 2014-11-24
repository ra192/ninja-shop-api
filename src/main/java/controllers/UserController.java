package controllers;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.persist.Transactional;
import dao.UserDao;
import dto.UserDto;
import filters.CorsFilter;
import model.User;
import ninja.FilterWith;
import ninja.Result;
import ninja.Results;
import ninja.jaxy.POST;
import ninja.jaxy.Path;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by yakov_000 on 17.11.2014.
 */
@Singleton
@FilterWith(CorsFilter.class)
@Path("/users")
public class UserController {
    Logger logger= LoggerFactory.getLogger(UserController.class);

    @Inject
    UserDao userDao;

    public static class FbUser {
        public Long id;
        public String email;
        public String first_name;
        public String last_name;
    }

    @Transactional
    @Path("/add.json")
    @POST
    public Result addUser(UserDto userDto) {
        final FbUser fbUser = getUserInfoFromFb(userDto.getAccessToken());

        if (fbUser==null) {
            return Results.json().render("error","Couldn't parse FB response to json");
        }

        if(!userDto.getUserID().equals(fbUser.id)) {
            logger.error("Specified userId and FB user id retrieved by access token doesn't match");
            return Results.json().render("error","Specified userId and FB user id retrieved by access token doesn't match");
        }

        User user = userDao.getByUserId(userDto.getUserID());

        if(user==null) {
            user=new User();
            user.setUserId(userDto.getUserID());

            Set<String>roles=new HashSet<>();
            roles.add("ROLE_USER");
            user.setRoles(roles);
        }

        user.setAccessToken(userDto.getAccessToken());
        user.setEmail(fbUser.email);
        user.setExpiresIn(userDto.getExpiresIn());
        user.setFirstName(fbUser.first_name);
        user.setLastName(fbUser.last_name);

        userDao.save(user);

        return Results.json().render("result","ok");
    }

    private FbUser getUserInfoFromFb(String acessToken) {

        final CloseableHttpClient httpClient = HttpClients.createDefault();
        final HttpGet httpGet = new HttpGet("https://graph.facebook.com/v2.2/me?access_token=" + acessToken);

        try {
            final CloseableHttpResponse httpResponse = httpClient.execute(httpGet);

            final ObjectMapper mapper = new ObjectMapper();
            mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

            return mapper.readValue(EntityUtils.toString(httpResponse.getEntity()), FbUser.class);
        } catch (IOException e) {
            logger.error("Couldn't retrieve user data from FB: ",e);
            return null;
        } finally {
            try {
                httpClient.close();
            } catch (IOException e) {
                logger.error("Couldn't close http client:", e);
            }
        }
    }
}
