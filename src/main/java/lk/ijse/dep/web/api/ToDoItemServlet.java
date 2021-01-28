package lk.ijse.dep.web.api;


import com.sun.xml.internal.ws.api.ha.StickyFeature;
import lk.ijse.dep.web.dto.ToDoItemDTO;
import lk.ijse.dep.web.util.Priority;
import lk.ijse.dep.web.util.Status;
import org.apache.commons.dbcp2.BasicDataSource;

import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.json.bind.JsonbException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@WebServlet
public class ToDoItemServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        BasicDataSource cp = (BasicDataSource) getServletContext().getAttribute("cp");
        Jsonb jsonb = JsonbBuilder.create();
        if(req.getPathInfo()==null||req.getPathInfo().equals("/")){
            try(Connection connection =cp.getConnection()) {
                PreparedStatement pstm1= connection.prepareStatement("SELECT * FROM to_do_item WHERE username = ?");
                pstm1.setObject(1,req.getAttribute("username"));
                ResultSet rst1= pstm1.executeQuery();
                List<ToDoItemDTO> items= new ArrayList<>();
                while(rst1.next()){
                    items.add(new ToDoItemDTO(rst1.getInt(1),
                            rst1.getString(2),
                            Priority.valueOf(rst1.getString(3)),
                            Status.valueOf(rst1.getString(4)),
                            rst1.getString(5)));
                }

                resp.setContentType("applicatio/json");
                resp.getWriter().println(jsonb.toJson(items));


            } catch (SQLException throwables) {
                throwables.printStackTrace();
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }

        }else{
            try(Connection connection= cp.getConnection()){
                int id =Integer.parseInt(req.getPathInfo().replace("/",""));


                PreparedStatement pstm2 = connection.prepareStatement("SELECT * FROM to_do_item where id=? AND username=?");
                pstm2.setObject(1,id);
                pstm2.setObject(2,req.getAttribute("user"));
                ResultSet rst2=pstm2.executeQuery();
                if(!rst2.next()){
                    resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                }else{
                    resp.setContentType("application/json");
                    ToDoItemDTO item= new ToDoItemDTO(rst2.getInt(1),
                            rst2.getString(2),
                            Priority.valueOf(rst2.getString(3)),
                            Status.valueOf(rst2.getString(4)),
                            rst2.getString(5));
                    resp.getWriter().println(jsonb.toJson(item));
                }


            } catch (SQLException throwables) {
                throwables.printStackTrace();
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }

        }



    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Jsonb jsonb =JsonbBuilder.create();
        try{
            ToDoItemDTO item= jsonb.fromJson(req.getReader(),ToDoItemDTO.class);

            if(item.getId()!=null || item.getText()==null|| item.getUsername()==null|| item.getText().trim().isEmpty()||item.getUsername().trim().isEmpty()){

                resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
                return;

            }

            BasicDataSource cp= (BasicDataSource) getServletContext().getAttribute("cp");
            try( Connection connection = cp.getConnection()){
                PreparedStatement pstm1=connection.prepareStatement("SELECT * FROM  `user` where username=? ");
                pstm1.setObject(1,item.getUsername());
               ResultSet rst1= pstm1.executeQuery();

               if(!rst1.next()){
                   resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
                   resp.setContentType("text/plain");
                   resp.getWriter().println("Invalid User");
                   return;
               }

               pstm1=connection.prepareStatement("Insert into to_do_item (`text`, `priority`, `status`, `username`) VALUES (?,?,?,?)", Statement.RETURN_GENERATED_KEYS);
               pstm1.setObject(1,item.getText());
               pstm1.setObject(2,item.getPriority().toString());
               pstm1.setObject(3,item.getStatus().toString());
               pstm1.setObject(4,item.getUsername());

               if(pstm1.executeUpdate()>0){
                   resp.setStatus(HttpServletResponse.SC_CREATED);
                   ResultSet generatedKeys= pstm1.getGeneratedKeys();
                   generatedKeys.next();
                   int generatedId=generatedKeys.getInt(1);
                    item.setId(generatedId);
                    resp.setContentType("application/json");
                    resp.getWriter().println(jsonb.toJson(item));
               }else{
                   resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
               }
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }catch (JsonbException e){
            e.printStackTrace();
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

        }
    }
}
