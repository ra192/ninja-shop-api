/**
 * Copyright (C) 2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package conf;


import controllers.*;
import ninja.AssetsController;
import ninja.Router;
import ninja.application.ApplicationRoutes;

public class Routes implements ApplicationRoutes {

    @Override
    public void init(Router router) {  
        
        router.GET().route("/").with(ApplicationController.class, "index");
        router.GET().route("/hello_world.json").with(ApplicationController.class, "helloWorldJson");

        router.POST().route("/products/{categoryName}.json").with(ProductController.class,"products");
        router.GET().route("/product/{id}.json").with(ProductController.class,"product");

        router.POST().route("/productsProperties/{categoryName}.json").with(ProductController.class,"properties");

        router.GET().route("/categories.json").with(CategoryController.class,"categories");

        router.POST().route("/users/add.json").with(UserController.class,"addUser");

        router.POST().route("/tools/import.json").with(ToolsController.class,"importData");
 
        ///////////////////////////////////////////////////////////////////////
        // Assets (pictures / javascript)
        ///////////////////////////////////////////////////////////////////////    
        router.GET().route("/assets/webjars/{fileName: .*}").with(AssetsController.class, "serveWebJars");
        router.GET().route("/assets/{fileName: .*}").with(AssetsController.class, "serveStatic");
        
        ///////////////////////////////////////////////////////////////////////
        // Index / Catchall shows index page
        ///////////////////////////////////////////////////////////////////////
        router.GET().route("/.*").with(ApplicationController.class, "index");
        router.OPTIONS().route("/.*").with(OptionsRequestController.class,"options");
    }

}
