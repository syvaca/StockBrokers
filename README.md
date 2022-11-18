# StockBrokers

Create a program that mimic trades that occur in a stock market by pulling current stock prices and using a trade schedule. I am able to parse a csv file that holds the schedule of trades requested to be performed, as well as the number of stock traders we have (including their individual budgets). After ensuring accurate parsing of both files the individual stock brokers work together to try and complete as many trades in the schedule as possible to maximize their collective stock portfolio. The sequence of trades preformed by individual stock brokers will be listed in the clients respective console.

Additionally, in order to collect the current price of each stock, I pull real data from FINNHUB API.

The main topics in this project include networking, multi-threading, concurrency issues, and API Querying.

All information in the input files can be determined/changed by the user, just make sure it stays in the same directory!
