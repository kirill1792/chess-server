package ru.kirill.chess.dao;

import ru.kirill.chess.model.ChessGame;
import ru.kirill.chess.model.User;

import java.sql.*;
import java.util.ArrayList;

public class UserDao {
    private Connection connection;
    private final String tableName = "user";

    public UserDao() {
        try {
            connect("jdbc:postgresql://188.225.75.220:19247/kirill_test", "extremenet", "123");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private User mapToObject(ResultSet rs) throws Exception {
        return new User(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("vkid"),
                rs.getInt("rating")
        );
    }

    private void connect(String url, String user, String password) throws Exception {
        Class.forName("org.postgresql.Driver");
        connection = DriverManager.getConnection(url, user, password);
    }

    public ArrayList<User> findUserByVkid(String vkid) {
        ArrayList<User> arr = new ArrayList<>();
        try (Statement statement = connection.createStatement()) {
            ResultSet rs = statement.executeQuery(
                    String.format("select * from user_account where vkid = '%s'", vkid));
            while (rs.next()) {
                arr.add(mapToObject(rs));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return arr;
    }

    public ArrayList<ChessGame> findUserGames(int id){
        ArrayList<ChessGame> arr = new ArrayList<>();
        try (Statement statement = connection.createStatement()) {
            ResultSet rs = statement.executeQuery("""
                    select g.id, g.datetime, uw.id uw_id, uw.name winner_name, uw.rating uw_rating,
                           ul.id ul_id, ul.name loser_name, ul.rating ul_rating, g.movedfirst moved_first,
                           g.moves game_moves, g.tie is_tie
                    from games g
                    left join user_account uw on g.winner = uw.id
                    left join user_account ul on g.loser = ul.id order by id desc""");
            while (rs.next()) {
                arr.add(mapGame(rs));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return arr;
    }

    private ChessGame mapGame(ResultSet rs) throws SQLException {
        return new ChessGame(
                rs.getInt("id"),
                rs.getString("datetime"),
                new User(
                        rs.getInt("uw_id"),
                        rs.getString("winner_name"),
                        null,
                        rs.getInt("uw_rating")
                ),
                new User(
                        rs.getInt("ul_id"),
                        rs.getString("loser_name"),
                        null,
                        rs.getInt("ul_rating")
                ),
                rs.getInt("moved_first"),
                rs.getString("game_moves"),
                rs.getBoolean("is_tie")
        );
    }

    public ArrayList<User> findUserById(int id) {
        ArrayList<User> arr = new ArrayList<>();
        try (Statement statement = connection.createStatement()) {
            ResultSet rs = statement.executeQuery(
                    String.format("select * from user_account where id = %d", id));
            while (rs.next()) {
                arr.add(mapToObject(rs));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return arr;
    }

    public void updateRating(int id, int rating) throws SQLException {
        Statement statement = connection.createStatement();
        statement.executeUpdate(String.format("update user_account set rating=%d where id=%d", rating, id));
    }

    public void insertUser(User user) throws SQLException {
        Statement statement = connection.createStatement();
        statement.executeUpdate(String.format("INSERT INTO user_account(name, vkid, rating) VALUES ( '%s', '%s', %d)", user.getName(), user.getVkid(), user.getRating()));
    }



    public void insertGame(ChessGame game) throws SQLException {
        Statement statement = connection.createStatement();
        statement.executeUpdate(String.format(
                "INSERT INTO games(datetime, winner, loser, movedfirst, moves, tie) " +
                        "VALUES ( '%s', %d, %d, %d, '%s', %b)",
                game.getDate(), game.getWinner().getId(), game.getLoser().getId(), game.getMovedFirst(), game.getMoves(), game.isTie()));
    }
}
