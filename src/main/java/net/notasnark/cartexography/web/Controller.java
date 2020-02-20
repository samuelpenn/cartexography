/*
 * Copyright (c) 2020, Samuel Penn (sam@notasnark.net).
 * See the file LICENSE at the root of the project.
 */
package net.notasnark.cartexography.web;

import com.google.gson.Gson;
import net.notasnark.cartexography.web.api.ApiException;
import net.notasnark.utils.SimpleImage;
import spark.Request;
import spark.ResponseTransformer;

import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.http.Part;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

public abstract class Controller {
    /**
     * Abstract method, must be implemented by a subclass.
     */
    public abstract void setupEndpoints();

    public static String toJson(Object object) {
        return new Gson().toJson(object);
    }

    public static ResponseTransformer json() {
        return Controller::toJson;
    }


    protected String readFormString(Request request, String name) throws IOException, ServletException {
        Part part = request.raw().getPart(name);

        if (part == null) {
            return null;
        }

        return new BufferedReader(new InputStreamReader(part.getInputStream())).lines().collect(Collectors.joining("\n"));
    }

    protected int readFormInt(Request request, String name) throws IOException, ServletException {
        String text = readFormString(request, name);

        if (text == null) {
            return 0;
        }

        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    protected SimpleImage readFormImage(Request request, String name) throws IOException, ServletException {
        Part part = request.raw().getPart(name);

        if (part == null) {
            return null;
        }

        InputStream is = part.getInputStream();
        return new SimpleImage(ImageIO.read(is));
    }

    /**
     * Gets the value of a parameter from the request object. First looks on the request path, and
     * then looks in the query parameters. Doesn't matter where the parameter was found, as long
     * as it was specified.
     *
     * @param request   HTTP Request object.
     * @param name      Name of the parameter.
     * @return          Non-null and non-empty value of the parameter.
     * @throws ApiException     Thrown if parameter not set or was empty.
     */
    public static String getStringParam(Request request, String name) throws ApiException {
        String value = request.params(":" + name);

        if (value == null || value.trim().length() == 0) {
            value = request.queryParams(name);
            if (value == null || value.trim().length() == 0) {
                throw new ApiException(400, ApiException.ApiErrorType.MISSING,
                        String.format("Parameter '%s' must be specified", name));
            }
        }
        return value;
    }

    public static String getStringParamWithDefault(Request request, String name, String value) throws ApiException {
        try {
            return getStringParam(request, name);
        } catch (ApiException e) {
            if (e.getErrorType() == ApiException.ApiErrorType.MISSING) {
                return value;
            }
            throw e;
        }
    }

    /**
     * Validates and returns an integer parameter from the request with the given name.
     * An exception is thrown if the parameter has not been set, or is of an invalid
     * format.
     *
     * @param request   HTTP Request object.
     * @param name      Name of the parameter.
     * @return          Guaranteed to be an integer.
     */
    public static int getIntParam(Request request, String name) throws ApiException {
        String value = getStringParam(request, name);
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new ApiException(400, ApiException.ApiErrorType.WRONG_FORMAT,
                    String.format("Parameter '%s' has illegal value [%s], must be a number", name, value));
        }
    }

    /**
     * Validates and returns an id from the request with the given name.
     * An exception is thrown if the parameter has not been set, or is not
     * a valid positive integer.
     *
     * @param request   HTTP Request object.
     * @param name      Name of the parameter.
     * @return          Guaranteed to be a strictly positive integer.
     */
    public static int getIdParam(Request request, String name) throws ApiException {
        int id = getIntParam(request, name);

        if (id < 1) {
            throw new ApiException(400, ApiException.ApiErrorType.OUT_OF_BOUNDS,
                    String.format("Parameter '%s' has illegal value [%d], must be a positive id", name, id));
        }

        return id;
    }

    /**
     * Validates and returns an integer from the request from the given named parameter. If no
     * parameter was defined, then return a default value instead. If a badly formatted parameter
     * was given, still returns an error.
     *
     * @param request   HTTP Request object.
     * @param name      Name of the parameter.
     * @param value     Default value.
     * @return          Guaranteed to be an integer.
     * @throws ApiException Exception if parameter was an illegal format.
     */
    public static int getIntParamWithDefault(Request request, String name, int value) throws ApiException {
        try {
            return getIntParam(request, name);
        } catch (ApiException e) {
            if (e.getErrorType() == ApiException.ApiErrorType.MISSING) {
                return value;
            }
            throw e;
        }
    }

    /**
     * Validates and returns a long parameter from the request with the given name.
     * An exception is thrown if the parameter has not been set, or is of an invalid
     * format.
     *
     * @param request   HTTP Request object.
     * @param name      Name of the parameter.
     * @return          Guaranteed to be a long.
     * @throws ApiException Exception if parameter was an illegal format or missing.
     */
    public static long getLongParam(Request request, String name) throws ApiException {
        String value = getStringParam(request, name);
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            throw new ApiException(400, ApiException.ApiErrorType.WRONG_FORMAT,
                    String.format("Parameter '%s' has illegal value [%s], must be a number", name, value));
        }
    }

    /**
     * Validates and returns a long from the request from the given named parameter. If no
     * parameter was defined, then return a default value instead. If a badly formatted
     * parameter was given, still returns an error.
     *
     * @param request   HTTP Request object.
     * @param name      Name of the parameter.
     * @param value     Default value if no parameter was set.
     * @return          Long value if set, default value otherwise.
     * @throws ApiException Exception if parameter was provided in invalid format.
     */
    public static long getLongParamWithDefault(Request request, String name, long value) throws ApiException {
        try {
            return getLongParam(request, name);
        } catch (ApiException e) {
            if (e.getErrorType() == ApiException.ApiErrorType.MISSING) {
                return value;
            }
            throw e;
        }
    }

    /**
     * Validates and returns a boolean from the given named parameter.
     *
     * @param request   HTTP Request object.
     * @param name      Name of the parameter.
     * @return          True or False.
     * @throws ApiException Exception if parameter was missing or invalid.
     */
    public static boolean getBooleanParam(Request request, String name) throws ApiException {
        String value = getStringParam(request, name);

        return Boolean.valueOf(value);
    }

    /**
     * Validates and returns a boolean from the given named parameter. If no parameter was specified
     * then the default value is returned instead.
     *
     * @param request   HTTP Request object.
     * @param name      Name of the parameter.
     * @param value     Default value.
     * @return          True or False.
     * @throws ApiException Exception if parameter was missing or invalid.
     */
    public static boolean getBooleanParamWithDefault(Request request, String name, boolean value) throws ApiException {
        try {
            return getBooleanParam(request, name);
        } catch (ApiException e) {
            if (e.getErrorType() == ApiException.ApiErrorType.MISSING) {
                return value;
            }
            throw e;
        }
    }
}
