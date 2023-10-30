# Bible verse lookup

1. Learn how to use Spring Boot, Spring JDBC template and Apache Lucene API.
2. Learn Oracle and PostgreSQL database.
3. Learn ReactJs and Ant Design library. 
3. Deploy in Oracle Cloud.

# Motivation

Everyone brings their own Bible during Bible Study. So from time to time, we've tried to figure out the correct verse and translation. Other times, we wanted to figure out where a particular verse or phrase was. 

This Bible verse search application tries to perform the following features:

1. Provide a user-friendly responsive interface to allow the user to
   look up a Bible verse in difference languages/versions
   (English ASV, Chinese Simple, Chinese Traditional)
2. Provides some knowledge like how many times a verse occurs in the Bible along with chapter and verse # details.

# Main feature

1. Load bible csv files (3 versions) into Oracle and Postgres database.
2. Index the bible csv files with apache Lucene java library.
3. REST API to query the bible with both sql and lucene search api
4. Front end interface with Reactjs to allow users to search and read the bible verse.
5. Set up in Oracle client live at https://lookup.searchwit.com

md stands for medical doctor and markdown

# curl command:
curl http://localhost:8080/api/bible/get10
# java version config
sudo update-alternatives --config java
