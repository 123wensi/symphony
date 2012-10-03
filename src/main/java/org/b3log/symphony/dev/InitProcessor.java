/*
 * Copyright (c) 2012, B3log Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.b3log.symphony.dev;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.b3log.latke.Keys;
import org.b3log.latke.Latkes;
import org.b3log.latke.model.Role;
import org.b3log.latke.model.User;
import org.b3log.latke.repository.Transaction;
import org.b3log.latke.repository.jdbc.util.JdbcRepositories;
import org.b3log.latke.servlet.HTTPRequestContext;
import org.b3log.latke.servlet.HTTPRequestMethod;
import org.b3log.latke.servlet.annotation.RequestProcessing;
import org.b3log.latke.servlet.annotation.RequestProcessor;
import org.b3log.symphony.model.Article;
import org.b3log.symphony.model.Statistic;
import org.b3log.symphony.repository.StatisticRepository;
import org.b3log.symphony.repository.UserRepository;
import org.b3log.symphony.service.ArticleMgmtService;
import org.b3log.symphony.service.UserMgmtService;
import org.json.JSONObject;

/**
 * Initializes database.
 *
 * @author <a href="mailto:DL88250@gmail.com">Liang Ding</a>
 * @version 1.0.0.2, Oct 2, 2012
 * @since 0.2.0
 */
@RequestProcessor
public class InitProcessor {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(InitProcessor.class.getName());

    /**
     * Generates tables.
     * 
     * @param context the specified context
     * @param request the specified request
     * @param response the specified response
     * @throws IOException io exception 
     */
    @RequestProcessing(value = "/dev/db/table/gen", method = HTTPRequestMethod.GET)
    public void genTables(final HTTPRequestContext context, final HttpServletRequest request, final HttpServletResponse response)
            throws IOException {
        try {
            LOGGER.log(Level.INFO, "Database [{0}], creates all tables", Latkes.getRuntimeDatabase());

            final List<JdbcRepositories.CreateTableResult> createTableResults = JdbcRepositories.initAllTables();
            for (final JdbcRepositories.CreateTableResult createTableResult : createTableResults) {
                LOGGER.log(Level.INFO, "Creates table result[tableName={0}, isSuccess={1}]",
                        new Object[]{createTableResult.getName(), createTableResult.isSuccess()});
            }

            // Init stat.
            final StatisticRepository statisticRepository = StatisticRepository.getInstance();
            final Transaction transaction = statisticRepository.beginTransaction();
            final JSONObject statistic = new JSONObject();
            statistic.put(Keys.OBJECT_ID, Statistic.STATISTIC);
            statistic.put(Statistic.STATISTIC_MEMBER_COUNT, 0);
            statisticRepository.add(statistic);
            transaction.commit();

            // Init admin
            final UserMgmtService userMgmtService = UserMgmtService.getInstance();
            final JSONObject admin = new JSONObject();
            admin.put(User.USER_EMAIL, "88250@b3log.org");
            admin.put(User.USER_NAME, "Admin");
            admin.put(User.USER_PASSWORD, "test");
            admin.put(User.USER_ROLE, Role.ADMIN_ROLE);
            userMgmtService.addUser(admin);

            final ArticleMgmtService articleMgmtService = ArticleMgmtService.getInstance();
            final JSONObject article = new JSONObject();
            article.put(Article.ARTICLE_TITLE, "你好，世界！");
            article.put(Article.ARTICLE_TAGS, "B3log, Java");
            article.put(Article.ARTICLE_CONTENT, "B3log Symphony 第一帖 ;-p");
            article.put(Article.ARTICLE_EDITOR_TYPE, 0);
            article.put(Article.ARTICLE_AUTHOR_EMAIL, "88250@b3log.org");
            article.put(Article.ARTICLE_AUTHOR_ID, admin.optString(Keys.OBJECT_ID));
            articleMgmtService.addArticle(article);

            response.sendRedirect("/");
        } catch (final Exception e) {
            LOGGER.log(Level.SEVERE, "Creates database tables failed", e);
            throw new IOException("Creates database tables failed", e);
        }
    }
}
