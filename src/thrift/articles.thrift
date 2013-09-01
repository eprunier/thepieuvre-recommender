namespace java thepieuvre.articles.service

struct User {
  1: string login
}

struct Article {
  1: string id,
  2: i32 like
}

service ArticlesService {
  void addReadArticle(1:User user, 2:Article article),
  list<Article> getReadArticles(1:User user)
}
