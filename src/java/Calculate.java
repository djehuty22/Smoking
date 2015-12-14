import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 *
 * @author Mike
 */
public class Calculate extends HttpServlet {
    
    private enum UnitOfTime {SECOND, MINUTE, HOUR, DAY, WEEK, MONTH, YEAR;}
    
    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        
        try {
            // timeline
            long startDate = dateFormat.parse(request.getParameter("startDate").replace("T", " ")).getTime();
            long endDate = dateFormat.parse(request.getParameter("endDate").replace("T", " ")).getTime();
            Date start = new Date(startDate);
            Date end = new Date(endDate);
            long durationInMilliseconds = endDate - startDate;
            
            // smoking frequency
            int cigarettesPerDay = Integer.parseInt(request.getParameter("cigarettes-daily"));
            double cigarettesPerMillisecond = getCigarettesPerMillisecond(cigarettesPerDay);
            
            // cost
            double cigarettesSmoked = cigarettesPerMillisecond * durationInMilliseconds;
            double cigaretteCost = 0.3;
            int packsSmoked = (int)Math.round(cigarettesSmoked) / 20;
            String dollarsSpent = getCostOfSmoking((int)Math.round(cigarettesSmoked), cigaretteCost);
            
            // time spent            
            HashMap timeSpentBreakdown = getBrokenDownTime(durationInMilliseconds);
            String lifeSpent = brokenTimeToSentence(timeSpentBreakdown);
            
            // time lost
            int timeLostPerCigarette = 600000; // 10 minutes in milliseconds
            long totalMillisecondsLost = Math.round(timeLostPerCigarette * cigarettesSmoked);
            HashMap lifeLostBreakdown = getBrokenDownTime(totalMillisecondsLost);
            String lifeLost = brokenTimeToSentence(lifeLostBreakdown);
            
            // Life to be gained
            long lifeExpectancyInMilliseconds = 2522880000000L; // 80 years in milliseconds
            long birthDate = dateFormat.parse(request.getParameter("birth-date").replace("T", " ")).getTime();
            long age = System.currentTimeMillis() - birthDate;
            int lifeExpectancy = (int)Math.ceil(convertMilliseconds(lifeExpectancyInMilliseconds, UnitOfTime.YEAR));
            String lifeLostSmokingToLifeExpectancy = brokenTimeToSentence(getBrokenDownTime(getTimeLostSmokingToLifeExpectancy(startDate, age, birthDate, cigarettesPerMillisecond)));
            
            try (PrintWriter out = response.getWriter()) {
                out.println("<!DOCTYPE html>");
                out.println("<html>");
                out.println("<head>");
                out.println("<title>Test Servlet</title>");            
                out.println("</head>");
                out.println("<body>");
                
                
                out.println("<p>Start date: " + start + "</p>");
                out.println("<p>End date: " + end + "</p>");
                out.println("<p>Cigarettes Daily: " + cigarettesPerDay + "</p>");
                
                
                out.println("<br />");
                
                
                out.println("<p>You've been smoking for " + lifeSpent + "</p>");
                out.println("<p>You've smoked about " + Math.round(cigarettesSmoked) + " cigarettes or about " + packsSmoked + " packs and (assuming $6.00/pack) spent about " + dollarsSpent + "</p>");
                out.println("<p>You've lost " + lifeLost + " of your life from smoking cigarettes");
                 
                
                out.println("<br />");
                
                
                out.println("<p>If you continue to smoke until you're " + lifeExpectancy + " years old, you will miss out on " + lifeLostSmokingToLifeExpectancy + " of life</p>");
                
                out.println("<br />");
                out.println("<br />");
                out.println("<a href=\"/Smoking/\">Return to start</a>");
                out.println("</body>");
                out.println("</html>");                
             }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
    
    private static double getCigarettesPerMillisecond(int cigarettesPerDay) {
        long millisecondsPerDay = 86400000; // milliseconds per day
        return (double)cigarettesPerDay / millisecondsPerDay;
    }
    
    private static String getCostOfSmoking(int cigarettesSmoked, double cigaretteCost) {
        return java.text.NumberFormat.getCurrencyInstance().format(cigarettesSmoked * cigaretteCost);
    }
    
    private static long getTimeLostSmokingToLifeExpectancy(long startDate, long age, long lifeExpectancy, double cigarettesPerMillisecond) {
        double cigarettesSmoked = (startDate + lifeExpectancy - age) * cigarettesPerMillisecond;
        int timeLostPerCigarette = 600000; // 10 minutes in milliseconds
        return Math.round(cigarettesSmoked * timeLostPerCigarette);
    }
    
    private static String brokenTimeToSentence(HashMap brokenDownTime) {
        String timeSentence =
            brokenDownTime.get("years") + " years, " +
            brokenDownTime.get("months") + " months, " +
            brokenDownTime.get("weeks") + " weeks, " +
            brokenDownTime.get("days") + " days, " +
            brokenDownTime.get("hours") + " hours, " +
            brokenDownTime.get("minutes") + " minutes, and " +
            brokenDownTime.get("seconds") + " seconds";
        
        return timeSentence;
    }
    
    private static HashMap<String, Long> getBrokenDownTime(long milliseconds) {
        long remainingTime = (long)convertMilliseconds(milliseconds, UnitOfTime.SECOND);
        
        int secondsInMinute = 60;
        int minutesInHour = 60;
        int hoursInDay = 24;
        int daysInWeek = 7;
        double weeksInMonth = 4.348;
        int monthsInYear = 12;

        HashMap<String, Long> brokenDownTime = new HashMap<>();
        brokenDownTime.put("seconds", remainingTime % secondsInMinute);
        remainingTime /= secondsInMinute; // in minutes
        
        brokenDownTime.put("minutes", remainingTime % minutesInHour);
        remainingTime /= minutesInHour; // in hours
        
        brokenDownTime.put("hours", remainingTime % hoursInDay);
        remainingTime /= hoursInDay; // in days
        
        brokenDownTime.put("days", remainingTime % daysInWeek);
        remainingTime /= daysInWeek; // in weeks
        
        brokenDownTime.put("weeks", Math.round(remainingTime % weeksInMonth));
        remainingTime /= weeksInMonth; // roughly in months
        
        brokenDownTime.put("months", remainingTime % monthsInYear);
        remainingTime /= monthsInYear; // in years
        
        brokenDownTime.put("years", remainingTime);
        
        return brokenDownTime;
    }
    
    private static double convertMilliseconds(long milliseconds, UnitOfTime desiredUnit) {
        int millisecondsInSecond = 1000;
        int secondsInMinute = 60;
        int minutesInHour = 60;
        int hoursInDay = 24;
        int daysInWeek = 7;
        double weeksInMonth = 4.348;
        int monthsInYear = 12;
        switch (desiredUnit) {
            case SECOND: return milliseconds / millisecondsInSecond;
            case MINUTE: return milliseconds / millisecondsInSecond / secondsInMinute;
            case HOUR:   return milliseconds / millisecondsInSecond / secondsInMinute / minutesInHour;
            case DAY:    return milliseconds / millisecondsInSecond / secondsInMinute / minutesInHour / hoursInDay;
            case WEEK:   return milliseconds / millisecondsInSecond / secondsInMinute / minutesInHour / hoursInDay / daysInWeek;
            case MONTH:  return milliseconds / millisecondsInSecond / secondsInMinute / minutesInHour / hoursInDay / daysInWeek / weeksInMonth;
            case YEAR:   return milliseconds / millisecondsInSecond / secondsInMinute / minutesInHour / hoursInDay / daysInWeek / weeksInMonth / monthsInYear;
        }
        return 0;
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
