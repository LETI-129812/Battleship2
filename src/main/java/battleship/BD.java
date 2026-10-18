package battleship;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.PreparedStatement;

public class BD {
    private static final String URL = "jdbc:sqlite:batalhanaval.db";

    public static void iniciarBD() {

        String sql = "CREATE TABLE IF NOT EXISTS jogadas (" + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "pos_x INTEGER," + "pos_y INTEGER,"
                + "resultado TEXT" + ");";


     try(
    Connection conn = DriverManager.getConnection(URL);
    Statement stmt = conn.createStatement()){
         stmt.execute(sql);
         System.out.println("Base de dados iniciada");
     } catch (SQLException e) {
        System.out.println("Base não iniciada"+ e.getMessage());
     }
}

    public static void guardarJogada(IPosition jogada, String resultado){
        String sql = "INSERT INTO jogadas (pos_x, pos_y, resultado) VALUES (?,?,?)";

        try(Connection conn = DriverManager.getConnection(URL);
            PreparedStatement pstmt = conn.prepareStatement(sql)){

                pstmt.setInt(1, jogada.getRow());
                pstmt.setInt(2, jogada.getColumn());
                pstmt.setString(3, resultado);

            pstmt.executeUpdate();
            System.out.println("Jogada guardada");
        }catch (SQLException e){
            System.out.println("Erro ao guardar");
        }

    }
}
