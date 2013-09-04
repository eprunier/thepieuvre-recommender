namespace java recommender.service

struct Article {
  1: string id,
  2: bool read,
  3: byte like
}

service ArticlesService {
  /**
   * Returns a list of articles from a list of articles IDs.
   */
  list<Article> getArticles(1:string username, 2:list<string> articlesIDs),

  /**
   * Associates an article to the user.
   */
  void addArticle(1:string username, 2:Article article),

  /**
   * Returns all articles read by the user.
   */
  list<string> getReadArticles(1:string username),

  /**
   * Indicates whether the article was read by the user.
   */
  bool isRead(1:string username, 2:string articleID)
}
