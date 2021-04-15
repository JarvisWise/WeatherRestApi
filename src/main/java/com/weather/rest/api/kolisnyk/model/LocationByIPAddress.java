package com.weather.rest.api.kolisnyk.model;

import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.model.CityResponse;
import com.weather.rest.api.kolisnyk.controllers.AbstractController;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Class LocationByIPAdress is responsible for
 * getting ip by request and getting city be ip
 */

public class LocationByIPAddress {

    private static final String LOCALHOST_IPV4 = "127.0.0.1";
    private static final String LOCALHOST_IPV6 = "0:0:0:0:0:0:0:1";
    private static final String DB_PATH = "src/main/resources/GeoLite2-City.mmdb";
    private static final String DEFAULT_LOCATION = "london,uk";

    /**
     * This method responsible for returning
     * client ip from request
     *
     * @param request request with data for getting ip
     * @return ip if it is possible
     * @throws UnknownHostException failed getting localhost ip
     */

    public static String getClientIp(HttpServletRequest request) throws UnknownHostException {
        String ipAddress = request.getHeader("X-Forwarded-For");
        if (ipAddress == null || (ipAddress).isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("Proxy-Client-IP");
        }

        if (ipAddress == null || (ipAddress).isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("WL-Proxy-Client-IP");
        }

        if (ipAddress == null || (ipAddress).isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getRemoteAddr();
            if (LOCALHOST_IPV4.equals(ipAddress) || LOCALHOST_IPV6.equals(ipAddress)) {
                InetAddress inetAddress = InetAddress.getLocalHost();
                ipAddress = inetAddress.getHostAddress();
            }
        }

        if (!(ipAddress == null || (ipAddress).isEmpty())
                && ipAddress.length() > 15
                && ipAddress.indexOf(",") > 0) {
            ipAddress = ipAddress.substring(0, ipAddress.indexOf(","));
        }

        return ipAddress;
    }

    /**
     * This method responsible for returning
     * city from request
     *
     * @param request request with data for
     *                getting ip and city name
     * @return city name if it is possible to
     * get it from ip, otherwise it returns the
     * default city name
     */

    public static String getCityByIP(HttpServletRequest request) {

        try (DatabaseReader dbReader = new DatabaseReader.Builder(new File(DB_PATH)).build()) {

            String ip = getClientIp(request);
            InetAddress ipAddress = InetAddress.getByName(ip);
            CityResponse response = dbReader.city(ipAddress);

            String cityName = response.getCity().getName();

            if (cityName == null) {
                AbstractController.log.info("Getting location by ip failed");
                return DEFAULT_LOCATION;
            } else {
                return cityName;
            }
        } catch (Exception e) {
            AbstractController.log.info("Getting location by ip failed");
            return DEFAULT_LOCATION;
        }
    }
}
