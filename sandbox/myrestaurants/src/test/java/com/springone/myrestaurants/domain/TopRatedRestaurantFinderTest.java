package com.springone.myrestaurants.domain;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.neo4j.graphdb.Node;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.datastore.graph.neo4j.spi.node.Neo4jHelper;
import org.springframework.datastore.graph.neo4j.support.GraphDatabaseContext;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.BeforeTransaction;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;

/**
 * @author Michael Hunger
 * @since 02.10.2010
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"/META-INF/spring/applicationContext.xml"})
@Transactional
public class TopRatedRestaurantFinderTest {
    @Autowired
    private GraphDatabaseContext graphDatabaseContext;

    @PersistenceContext
    EntityManager em;

    @Autowired
    DataSource dataSource;

    @Autowired
    PlatformTransactionManager transactionManager;

    @Before
    @BeforeTransaction
    public void cleanDb() {
        Neo4jHelper.cleanDb(graphDatabaseContext);
    }

    @Test
    public void returnsFriendsInOrder() {
        final Restaurant a = restaurant("a");
        final Restaurant b = restaurant("b");
        final Restaurant c = restaurant("c");
        final UserAccount A = user("A");
        final UserAccount B1 = user("B1");
        final UserAccount B2 = user("B2");
        final UserAccount C1 = user("C1");
        Assert.assertNotNull("user has node", node(A));
        A.knows(B1);
        A.knows(B2);
        B1.knows(C1);
        C1.rate(a, 1, "");
        C1.rate(b, 3, "");
        C1.rate(c, 5, "");
        final Node node = node(A);
        final Collection<RatedRestaurant> topNRatedRestaurants = new TopRatedRestaurantFinder().getTopNRatedRestaurants(A, 5);
        Collection<Restaurant> result = new ArrayList<Restaurant>();
        for (RatedRestaurant ratedRestaurant : topNRatedRestaurants) {
            result.add(ratedRestaurant.getRestaurant());
        }
        Assert.assertEquals(asList(b, c, a), result);
    }

    private Node node(UserAccount a) {
        return a.getUnderlyingState();
    }

    private UserAccount user(String name) {
        UserAccount userAccount = new UserAccount();
        //userAccount.setId((long) name.hashCode());
        em.persist(userAccount);
        em.flush();
        userAccount.getId();
        userAccount.setNickname(name);
        return userAccount;
    }

    private Restaurant restaurant(String name) {
        Restaurant restaurant = new Restaurant();
        //restaurant.setId((long) name.hashCode());
        em.persist(restaurant);
        em.flush();
        restaurant.getId();
        return restaurant;
    }

    private void dumpResults(String sql) {
        final List<Map<String, Object>> result = new SimpleJdbcTemplate(dataSource).queryForList(sql);
        System.out.println("result = " + result);
    }
}
