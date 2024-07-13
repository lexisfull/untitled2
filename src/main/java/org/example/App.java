package org.example;

import org.example.entity.Post;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args ) {
        Configuration configuration = new Configuration();
        configuration.configure("hibernate.cfg.xml");

        try (SessionFactory sessionFactory = configuration.buildSessionFactory()) {
            Session session = sessionFactory.openSession();
            session.beginTransaction();

            addauthor("Полина", session);
            addauthor("Соня", session);
            addauthor("Петр", session);
            addauthor("Иван", session);

            Author author1 = getAuthorById(1L, session);
            Author author2 = getAuthorById(2L, session);
            Author author3 = getAuthorById(3L, session);
            Author author4 = getAuthorById(4L, session);

            addPost(author1, "Пост о красоте", session);
            addPost(author2, "Пост о спорте", session);
            addPost(author3, "Пост о туризме", session);
            addPost(author4, "Пост о природе", session);
            addPost(author1, "Пост о программировании", session);

            Post post1 = findPostById(1L, session);
            Post post2 = findPostById(2L, session);
            Post post3 = findPostById(3L, session);

            addCommentToPost(author1, post2, "важный пост", session);
            addCommentToPost(author1, post2, "важный пост", session);
            addCommentToPost(author2, post1, "хороший пост", session);
            addCommentToPost(author3, post1, "хороший пост", session);
            addCommentToPost(author1, post3, "отличный пост", session);
            addCommentToPost(author2, post3, "отличный пост", session);
            addCommentToPost(author4, post3, "отличный пост", session);

            System.out.println("Все посты по автору " + getAllPostsByUser(author1, session));

            System.out.println("Все коментарии к публикации " + getAllCommentByPost(post1, session));

            System.out.println("Все коментарии от автора " + getAllCommentByUser(author1, session));

            System.out.println("По идентификатору автора загрузить автора, под чьими публикациями он оставлял комменты." +
                    getAllUsersByReactionOfAnother(author1, session));


            deletePost(findPostById(5L, session), session);
            session.getTransaction().commit();
        }
    }

    private static List<Author> getAllUsersByReactionOfAnother(Author author, Session session) {
        List<PostComment> comments = getAllCommentByUser(author, session);

        Set<Post> posts = new HashSet<>();
        for (PostComment comment : comments) {
            posts.add(comment.getPost());
        }

        Set<Author> autchorReacted = new HashSet<>();
        for (Post post : posts) {
            List<PostComment> postComments = getAllCommentByPost(post, session);
            for (PostComment postComment : postComments) {
                Author commenter = postComment.getAuthor();
                if (!commenter.equals(author)) {
                    autchorReacted.add(commenter);
                }
            }
        }

        return new ArrayList<>(autchorReacted);
    }

    private static List<PostComment> getAllCommentByUser(Author author, Session session) {
        return session.createQuery("from PostComment where author= :author")
                .setParameter("author", author)
                .list();
    }

    private static List<PostComment> getAllCommentByPost(Post post, Session session) {
        return session.createQuery("from PostComment where post= :post")
                .setParameter("post", post)
                .list();
    }

    private static List<Post> getAllPostsByUser(Author author, Session session) {
        return session.createQuery("from Post where author= :author")
                .setParameter("author", author).list();
    }

    private static void addauthor(String name, Session session) {
        Author author = new Author();
        author.setName(name);
        session.persist(author);
    }

    private static Author getAuthorById(Long id, Session session) {
        return session.get(Author.class, id);
    }

    private static void addPost(Author author, String title, Session session) {
        Post post = new Post();
        post.setTitle(title);
        post.setAuthor(author);
        post.setTimestamp(LocalDateTime.now());


        session.persist(post);

    }

    private static Post findPostById(Long id, Session session) {
        return session.get(Post.class, id);
    }

    private static List<Post> findAllPosts(Session session) {
        return session.createQuery("from Post").list();
    }

    private static void deletePost(Post post, Session session) {
        deleteAllCommentFromPost(post, session);
        session.remove(post);
    }


    private static void addCommentToPost(Author author, Post post, String text, Session session) {
        PostComment comment = new PostComment();
        comment.setAuthor(author);
        comment.setPost(post);
        comment.setText(text);
        comment.setTimestamp(LocalDateTime.now());
        session.persist(comment);
    }

    private static PostComment findCommentById(Long id, Session session) {
        return session.get(PostComment.class, id);
    }

    public static void deleteAllCommentFromPost(Post post, Session session) {
        session.createQuery("delete from PostComment where post = :post")
                .setParameter("post", post)
                .executeUpdate();
    }

    private static List<PostComment> findAllCommentByPost(Post post, Session session) {
        post = session.get(Post.class, post);
        return post.getComments();
    }

    private static void deleteComment(PostComment comment, Session session) {
        session.remove(comment);
    }

}
}
