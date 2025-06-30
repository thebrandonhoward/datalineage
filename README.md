```
20:37:18.418 [Executor task launch worker for task 0.0 in stage 3.0 (TID 3)] INFO org.apache.spark.sql.catalyst.expressions.codegen.CodeGenerator -- Code generated in 28.632 ms
User Defined Function Processing ageCaseWithLineage for field: 18
User Defined Function Processing coalesceWithLineage2 for names: [phone, alt_phone, na] and fields: [null, 555-1001, na] -> Matched: alt_phone
User Defined Function Processing ageCaseWithLineage for field: 18
User Defined Function Processing coalesceWithLineage2 for names: [phone, alt_phone, na] and fields: [555-1002, null, na] -> Matched: phone
User Defined Function Processing ageCaseWithLineage for field: 19
User Defined Function Processing coalesceWithLineage2 for names: [phone, alt_phone, na] and fields: [null, null, na] -> Matched: na
User Defined Function Processing ageCaseWithLineage for field: 19
User Defined Function Processing coalesceWithLineage2 for names: [phone, alt_phone, na] and fields: [555-1004, 555-2004, na] -> Matched: phone
User Defined Function Processing ageCaseWithLineage for field: 20
User Defined Function Processing coalesceWithLineage2 for names: [phone, alt_phone, na] and fields: [null, 555-1005, na] -> Matched: alt_phone
User Defined Function Processing ageCaseWithLineage for field: 21
User Defined Function Processing coalesceWithLineage2 for names: [phone, alt_phone, na] and fields: [555-1006, null, na] -> Matched: phone
User Defined Function Processing ageCaseWithLineage for field: 21
User Defined Function Processing coalesceWithLineage2 for names: [phone, alt_phone, na] and fields: [null, 555-1007, na] -> Matched: alt_phone
User Defined Function Processing ageCaseWithLineage for field: 22
User Defined Function Processing coalesceWithLineage2 for names: [phone, alt_phone, na] and fields: [null, null, na] -> Matched: na
User Defined Function Processing ageCaseWithLineage for field: 39
User Defined Function Processing coalesceWithLineage2 for names: [phone, alt_phone, na] and fields: [555-1009, 555-2009, na] -> Matched: phone
User Defined Function Processing ageCaseWithLineage for field: 45
User Defined Function Processing coalesceWithLineage2 for names: [phone, alt_phone, na] and fields: [null, 555-1010, na] -> Matched: alt_phone
20:37:18.437 [Executor task launch worker for task 0.0 in stage 3.0 (TID 3)] INFO org.apache.spark.executor.Executor -- Finished task 0.0 in stage 3.0 (TID 3). 2090 bytes result sent to driver
```

---

```json
{"OrderID":"123", "Quantity":"30", "QuantityText":"The quantity is 30"}
```

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