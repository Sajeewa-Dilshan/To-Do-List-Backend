package lk.ijse.dep.web.api;


import org.apache.commons.dbcp2.BasicDataSource;

import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebFilter;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@WebServlet(name="UserServlet",urlPatterns = {"/api/v1/users/*","/api/v1/auth"})
public class UserServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        String query =req.getParameter("q");
        if(query !=null){
           BasicDataSource bds = (BasicDataSource) getServletContext().getAttribute("cp");

           try( Connection connection = bds.getConnection()){

               PreparedStatement pstm1=connection.prepareStatement("SELECT username FROM `user` WHERE username = ?");
               pstm1.setObject(1,query);
               ResultSet rst1= pstm1.executeQuery();

               if(rst1.next()){
                   Jsonb jsonb = JsonbBuilder.create();
                   resp.setContentType("application/json");
                   resp.getWriter().println(jsonb.toJson(rst1.getString(1)));

               }else{
                   resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
               }

           } catch (SQLException throwables) {
               resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
               throwables.printStackTrace();
           }

        }



    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Jsonb jsonb =JsonbBuilder.create();

        try{




        }catch (){

        }
    }
}
