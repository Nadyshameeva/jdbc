package org.example.data.repo;

import org.example.data.core.JdbcRepository;
import org.example.model.Music;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MusicRepository extends JdbcRepository<Music, Integer> {
    public MusicRepository(Connection connection) {
        super(connection);
    }

    public List<Music> findAllByTitleMatchingRegex(String regex) {
        String sql = "select * from study.music where " + tableName + ".name !~* ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, regex);

            ResultSet rs = ps.executeQuery();
            List<Music> musics = new ArrayList<>();

            while (rs.next()) {
                Music music = mapRow(rs);
                musics.add(music);
            }

            return musics;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }
}
