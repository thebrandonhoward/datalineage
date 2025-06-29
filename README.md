```json
{"OrderID":"123", "Quantity":"30", "QuantityText":"The quantity is 30"}

```sql
SELECT OrderID, Quantity,
   CASE
   WHEN Quantity > 30 THEN 'The quantity is greater than 30'
   WHEN Quantity = 30 THEN 'The quantity is 30'
   ELSE 'The quantity is under 30'
   END AS QuantityText
FROM OrderDetails
```

```sql
SELECT {{OrderID::123}}, {{Quantity::30}}, CASE WHEN {{Quantity::30}} > 30 THEN 'The quantity is greater than 30' WHEN {{Quantity::30}} = 30 THEN 'The quantity is 30' ELSE 'The quantity is under 30' END AS {{QuantityText::The quantity is 30}} FROM OrderDetails
```

```sql
SELECT {{OrderID::123}}, {{Quantity::30}}, CASE WHEN {{Quantity::30}} > 30 THEN 'The quantity is greater than 30' WHEN {{Quantity::30}} = 30 THEN 'The quantity is 30' ELSE 'The quantity is under 30' END AS {{QuantityText::The quantity is 30}} FROM OrderDetails
```

true

Process finished with exit code 0