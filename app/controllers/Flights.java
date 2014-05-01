package controllers;

import play.*;
import play.mvc.*;
import play.data.validation.*;

import java.util.*;
import java.text.*;

import models.*;

public class Flights extends Application {
    
    // Interception methods
    // Invoked before for each action call of this controller class
    // Verifying if a user is authenticated
    @Before
    static void checkUser() {
        if(connected() == null) {
            flash.error("Please log in first");
            Application.index();
        }
    }
    
    // ~~~
    
    public static void index() {
        List<Booking> bookings = Booking.find("byUser", connected()).fetch();
        render(bookings);
    }

    /***TODO: Implement dep_date***/

    public static void list(String dep_city, String arrv_city, Date dep_date, Integer size, Integer page) {
        List<Flight> flights = null;
        page = page != null ? page : 1;
        if(dep_city.trim().length() == 0 || arrv_city.trim().length() == 0 || dep_date == null) {
            // return no more result: indicating there is no match
        } else {
            dep_city = dep_city.toLowerCase();
            arrv_city = arrv_city.toLowerCase();
            // System.out.println(dep_date);

            // flights = Flight.find("dep_date like ?", "2014-04-30").fetch(page, size);

            flights = Flight.find("lower(dep_city) like ? AND lower(arrv_city) like ? AND dep_date like ?",
             "%"+dep_city+"%", "%"+arrv_city+"%", dep_date).fetch(page, size);
        }
        render(flights, dep_city, arrv_city, dep_date, size, page);
    }
    
    public static void show(Long id) {
        Flight flight = Flight.findById(id);
        render(flight);
    }
    
    public static void book(Long id) {
        Flight flight = Flight.findById(id);
        render(flight);
    }
    
    public static void confirmBooking(Long id, Booking booking) {
        Flight flight = Flight.findById(id);
        booking.flight = flight;
        booking.user = connected();
        validation.valid(booking);
        
        // Errors or revise
        if(validation.hasErrors() || params.get("revise") != null) {
            render("@book", flight, booking);
        }
        
        // Confirm
        if(params.get("confirm") != null) {
            booking.save();
            flash.success("Thank you, %s! your confimation number for the flight is %s", connected().name, booking.id);
            index();
        }
        
        // Display booking
        render(flight, booking);
    }
    
    public static void cancelBooking(Long id) {
        Booking booking = Booking.findById(id);
        booking.delete();
        flash.success("Booking cancelled for confirmation number %s", booking.id);
        index();
    }
    
    public static void settings() {
        render();
    }
    
    public static void saveSettings(String password, String verifyPassword) {
        User connected = connected();
        connected.password = password;
        validation.valid(connected);
        validation.required(verifyPassword);
        validation.equals(verifyPassword, password).message("Your password doesn't match");
        if(validation.hasErrors()) {
            render("@settings", connected, verifyPassword);
        }
        connected.save();
        flash.success("Password updated");
        index();
    }

    // Show all flights with link to modify it
    /*public static void adminList() {
        List<Flight> flights = null;
        flights = Flight.findAll().fetch(page, size);
        Integer page = 1;
        Integer size = 10;
        list.html.render(flights, size, page);
    }*/
    
}

