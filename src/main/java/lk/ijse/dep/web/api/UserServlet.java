package lk.ijse.dep.web.api;


import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lk.ijse.dep.web.dto.UserDTO;
import lk.ijse.dep.web.util.AppUtil;
import org.apache.commons.codec.Decoder;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.dbcp2.BasicDataSource;

import javax.crypto.SecretKey;
import javax.json.JsonException;
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
import java.util.Date;

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
            UserDTO userDTO =jsonb.fromJson(req.getReader(),UserDTO.class);
            if(userDTO.getUsername()==null|| userDTO.getPassword()==null){
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
            BasicDataSource cp= (BasicDataSource) getServletContext().getAttribute("cp");
            try(Connection connection=cp.getConnection();){

                if(req.getServletPath().equals("/api/v1/auth")){
                    PreparedStatement pstm1 =connection.prepareStatement("SELECT * FROM `user` WHERE username=?");
                    pstm1.setObject(1,userDTO.getUsername());
                    ResultSet rst1= pstm1.executeQuery();
                    if(rst1.next()){
                         String sha256Hex= DigestUtils.sha256Hex(userDTO.getPassword());
                        if(sha256Hex.equals(rst1.getString(2))){
                            SecretKey key= Keys.hmacShaKeyFor(Decoders.BASE64URL.decode(AppUtil.getAppSecretKey()));
                            String jws= Jwts.builder()
                                    .setIssuer("ijse")
                                    .setExpiration(new Date(System.currentTimeMillis()+(1000*60*60*24)))
                                    .setIssuedAt(new Date())
                                    .claim("name",userDTO.getUsername())
                                    .signWith(key)
                                    .compact();
                            resp.setContentType("text/plain");
                            resp.getWriter().println(jws);

                        }else{
                            resp.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                        }
                    }else{
                        resp.sendError(HttpServletResponse.SC_UNAUTHORIZED);

                    }

                }else{
                    PreparedStatement pstm2= connection.prepareStatement("SELECT  * FROM  `user` WHERE username=?");
                    pstm2.setObject(1,userDTO.getPassword());
                    if( pstm2.executeQuery().next() ){
                        resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
                        resp.getWriter().println("User already exists");
                        return;
                    }


                    PreparedStatement pstm3= connection.prepareStatement("INSERT INTO `user` VALUES (?,?)");
                    pstm3.setObject(1,userDTO.getUsername());

                    String sha256Hex = DigestUtils.sha256Hex(Decoders.BASE64URL.decode(userDTO.getPassword()));

                    pstm3.setObject(2,sha256Hex);

                    if (pstm3.executeUpdate()>0){
                        resp.setStatus(HttpServletResponse.SC_CREATED);

                    }else {
                        resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    }
                }





            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }


        }catch (JsonException exp){
            exp.printStackTrace();
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
        }
    }
}
