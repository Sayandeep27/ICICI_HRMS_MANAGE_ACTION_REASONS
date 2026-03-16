# Manage Action Reasons API (BRD v4)

A Spring Boot + MSSQL implementation of the **Manage Action Reasons module** with Maker‑Checker workflow.

This README explains how another developer can:

1. Setup the database
2. Run the application
3. Test all APIs using Postman
4. Verify the data in SQL Server

---

# Base API URL

```
http://localhost:8080/api/v4/action-reasons
```

---

# API Testing Flow (Sequential)

Run the APIs **in this order**.

---

# 1. Create Action Reason

**Method**

```
POST
```

**Endpoint**

```
http://localhost:8080/api/v4/action-reasons
```

**Body**

```json
{
  "actionReasonName": "Role Change",
  "description": "Role Change Reason",
  "moduleId": 1,
  "moduleMasterId": 3,
  "effectiveStartDate": "2026-06-01",
  "effectiveEndDate": null,
  "remarks": "Initial creation",
  "createdBy": "EMP201"
}
```

**Expected Response**

```json
{
  "message": "Action Reason Created Successfully"
}
```

---

# 2. Create Another Record

**Method**

```
POST
```

**Endpoint**

```
http://localhost:8080/api/v4/action-reasons
```

**Body**

```json
{
  "actionReasonName": "Location Adjustment",
  "description": "Location Adjustment Reason",
  "moduleId": 1,
  "moduleMasterId": 2,
  "effectiveStartDate": "2026-07-01",
  "effectiveEndDate": null,
  "remarks": "Testing second record",
  "createdBy": "EMP202"
}
```

**Expected Response**

```json
{
  "message": "Action Reason Created Successfully"
}
```

---

# 3. Submit First Record

**Method**

```
POST
```

**Endpoint**

```
http://localhost:8080/api/v4/action-reasons/1/submit
```

**Expected Response**

```json
{
  "message": "Action Reason Submitted Successfully"
}
```

**Result**

Status becomes:

```
SUBMITTED
```

---

# 4. Approve First Record

**Method**

```
POST
```

**Endpoint**

```
http://localhost:8080/api/v4/action-reasons/1/approve
```

**Body**

```json
{
  "checkerId": "EMP900"
}
```

**Expected Response**

```json
{
  "message": "Action Reason Approved Successfully"
}
```

**Result**

Status becomes:

```
APPROVED
```

A history record is also created.

---

# 5. Update Second Record

**Method**

```
PUT
```

**Endpoint**

```
http://localhost:8080/api/v4/action-reasons/2
```

**Body**

```json
{
  "actionReasonName": "Location Adjusted",
  "description": "Updated Location Reason",
  "effectiveEndDate": "2027-01-01",
  "remarks": "Updated test",
  "modifiedBy": "EMP202"
}
```

**Expected Response**

```json
{
  "message": "Action Reason Updated Successfully"
}
```

---

# 6. Submit Second Record

**Method**

```
POST
```

**Endpoint**

```
http://localhost:8080/api/v4/action-reasons/2/submit
```

**Expected Response**

```json
{
  "message": "Action Reason Submitted Successfully"
}
```

---

# 7. Reject Second Record

**Method**

```
POST
```

**Endpoint**

```
http://localhost:8080/api/v4/action-reasons/2/reject
```

**Expected Response**

```json
{
  "message": "Action Reason Rejected Successfully"
}
```

**Result**

Status becomes:

```
REJECTED
```

---

# 8. Inactivate First Record

**Method**

```
POST
```

**Endpoint**

```
http://localhost:8080/api/v4/action-reasons/1/inactivate
```

**Expected Response**

```json
{
  "message": "Action Reason Inactivated Successfully"
}
```

**Result**

Status becomes:

```
INACTIVE
```

---

# 9. Search API

**Method**

```
POST
```

**Endpoint**

```
http://localhost:8080/api/v4/action-reasons/search
```

**Body**

```json
{
  "field": "actionReasonName",
  "value": "Location",
  "operator": "CONTAINS"
}
```

**Expected Response**

Returns matching records.

---

# 10. Get History

**Method**

```
GET
```

**Endpoint**

```
http://localhost:8080/api/v4/action-reasons/1/history
```

**Expected Response**

```json
[
  {
    "actionReasonId": 1,
    "actionReasonName": "Role Change",
    "actionReasonCode": "ROLE_CHANGE",
    "createdBy": "EMP201",
    "checkedBy": "EMP900"
  }
]
```

---

# Verify Data in SQL

Run the following queries to verify the stored data.

```
SELECT * FROM action_reason;

SELECT * FROM action_reason_history;
```

---

# Features Implemented

| Feature                 | Status      |
| ----------------------- | ----------- |
| Create Action Reason    | Implemented |
| Update Action Reason    | Implemented |
| Submit for Approval     | Implemented |
| Approve                 | Implemented |
| Reject                  | Implemented |
| Inactivate              | Implemented |
| Maker Checker Workflow  | Implemented |
| Change History          | Implemented |
| Search API              | Implemented |
| Version Tracking        | Implemented |
| Code Generation         | Implemented |
| Module Mapping          | Implemented |
| Effective Date Handling | Implemented |

---

# Technology Stack

| Layer      | Technology                  |
| ---------- | --------------------------- |
| Backend    | Spring Boot                 |
| Database   | Microsoft SQL Server        |
| ORM        | Spring Data JPA / Hibernate |
| Build Tool | Maven                       |
| Testing    | Postman                     |

---

# Notes

1. APIs follow **Maker → Checker workflow**.
2. `action_reason_code` is automatically generated from the name.
3. History is saved after approval.
4. Search API supports operators like `CONTAINS`, `STARTS_WITH`, etc.

---

**Project Module:** Manage Action Reasons (BRD v4)
