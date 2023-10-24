# Bible verse lookup

1. Learning how to use Spring Boot,  Spring Jdbc template and Apache Lucene API.
2. Learn Oracle and Postgresql database.
3. Deploy in the Oracle always free cloud.

# Motivation

From time to time during bible study, people try to look up the bible
with language and translation. Also people try to know how many times
the verse occurred in the bible, the chapter of the book
or the verse of the chapter.

This bible verse search application is try to answer the above two questions.

1. Provide a user-friendly responsive interface to allow user to
   look up verse of bible with difference languages
   (English ASV, Chinese Simple,Chinese Tradition)
2. Provide some insight like how many times the verse occurred of bible with chapter and verse number details.

# Main feature

1. Load bible csv files (3 versions) into Oracle and Postgres database.
2. Index the bible csv files with apache Lucene java library.
3. REST API to query the bible with both sql and lucene search api
4. Front end interface with Reactjs to allow users to search and read the bible verse.
5. Set up in Oracle client live at http://bible.searchwit.com

md stands for medical doctor and markdown
# curl command:
curl http://localhost:8080/api/bible/get10
# java version config
sudo update-alternatives --config java
