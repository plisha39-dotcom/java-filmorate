package ru.yandex.practicum.filmorate.storage;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Friendship;
import ru.yandex.practicum.filmorate.model.FriendshipStatus;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public class FriendshipDbStorage implements FriendshipStorage {
    private final JdbcTemplate jdbc;
    private final RowMapper<Friendship> rowMapper = ((rs, rowNum) -> {
        Friendship friendship = new Friendship();
        friendship.setRequesterId(rs.getLong("requester_id"));
        friendship.setAddresseeId(rs.getLong("addressee_id"));
        friendship.setStatus(FriendshipStatus.valueOf(rs.getString("status")));

        return friendship;
    });

    public FriendshipDbStorage(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public Optional<Friendship> findFriendship(Long requesterId, Long addresseeId) {
        String query = """
                select f.requester_id, f.addressee_id, fs.name as status
                from friendship f
                left join friendship_statuses fs on f.status_id = fs.status_id
                where f.requester_id = ? and f.addressee_id = ?""";
        try {
            return Optional.ofNullable(jdbc.queryForObject(query, rowMapper, requesterId, addresseeId));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Friendship addFriendship(Long requesterId, Long addresseeId) {
        String query = "insert into friendship (requester_id, addressee_id, status_id) values (?, ?, 1)";
        int rows = jdbc.update(query, requesterId, addresseeId);
        if (rows == 0) {
            throw new RuntimeException("Не удалось создать дружескую связь");
        }

        Friendship friendship = new Friendship();
        friendship.setRequesterId(requesterId);
        friendship.setAddresseeId(addresseeId);
        friendship.setStatus(FriendshipStatus.UNCONFIRMED);

        return friendship;
    }

    @Override
    public Friendship confirmFriendship(Long requesterId, Long addresseeId) {
        String query = "update friendship set status_id = 2 where requester_id = ? and addressee_id = ?";
        int rows = jdbc.update(query, requesterId, addresseeId);
        if (rows == 0) {
            throw new RuntimeException("Не удалось обновить статус");
        }

        Friendship friendship = new Friendship();
        friendship.setRequesterId(requesterId);
        friendship.setAddresseeId(addresseeId);
        friendship.setStatus(FriendshipStatus.CONFIRMED);

        return friendship;
    }

    @Override
    public void deleteFriendship(Long requesterId, Long addresseeId) {
        String query = "delete from friendship where requester_id = ? and addressee_id = ?";
        jdbc.update(query, requesterId, addresseeId);
    }

    @Override
    public Set<Long> getFriendsIds(Long userId) {
        String query = """
                select addressee_id as friend_id
                from friendship
                where requester_id = ?
                union
                select requester_id as friend_id
                from friendship
                where status_id = 2 and addressee_id = ?
                """;
        List<Long> friendIds = jdbc.query(query, (rs, rowNum) -> rs.getLong("friend_id"), userId, userId);
        return new HashSet<>(friendIds);
    }
}
