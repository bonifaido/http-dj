package undertow;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.servlet.GuiceFilter;
import com.google.inject.servlet.ServletModule;
import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.PathHandler;
import io.undertow.servlet.Servlets;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.DeploymentManager;

import javax.inject.Singleton;
import javax.servlet.DispatcherType;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static io.undertow.servlet.Servlets.filter;

public class UndertowServletExample {

    public static final String CONTEXT_PATH = "/mywebapp";

    public static void main(String[] args) throws ServletException {

        Injector injector = Guice.createInjector(new ServletModule() {
            @Override
            protected void configureServlets() {
                serve("/*").with(MyHttpServlet.class);
            }
        });

        DeploymentInfo deploymentInfo = Servlets.deployment()
                .setClassLoader(UndertowServletExample.class.getClassLoader())
                .setDeploymentName("testdeploy")
                .setContextPath(CONTEXT_PATH)
                .addFilter(filter("guice", GuiceFilter.class))
                .addFilterUrlMapping("guice", "/*", DispatcherType.REQUEST);

        DeploymentManager deploymentManager = Servlets.defaultContainer().addDeployment(deploymentInfo);
        deploymentManager.deploy();
        deploymentManager.getDeployment();

        HttpHandler servletHandler = deploymentManager.start();

        PathHandler path = Handlers
                .path(Handlers.redirect(CONTEXT_PATH))
                .addPrefixPath(CONTEXT_PATH, servletHandler);

        Undertow server = Undertow.builder()
                .addHttpListener(8080, "localhost")
                .setHandler(path)
                .build();

        server.start();
    }

    @Singleton
    public static class MyHttpServlet extends HttpServlet {
        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            resp.getWriter().append("Hello World");
        }
    }
}
