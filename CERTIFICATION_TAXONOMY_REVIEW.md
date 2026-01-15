# Certification Taxonomy Review

**Review Date:** 2025-12-27
**Total Certifications:** 46
**Status:** ⚠️ Needs Reorganization

---

## 📊 CURRENT STATE

### Certifications by Category:

| Category | Count | Certifications |
|----------|-------|----------------|
| **Development** | 12 | Backend, Frontend, React, Node, Python, Golang, API Design, System Design, DevOps, SQL, JavaScript/TS, Software Architecture |
| **Testing/QA** | 6 | ISTQB CTFL, Foundation v4.0, Agile, AI Testing, Technical Analyst, Agile Tester |
| **Cloud AWS** | 5 | SAA, DVA, DOP, SAP, Solutions Architect |
| **Agile/Scrum** | 4 | PSM I, PSM II, PSPO I, Agile Scrum Master |
| **Container/Infra** | 4 | Docker DCA, Kubernetes CKA/CKAD/Administrator, Terraform |
| **Business/PM** | 5 | CBAP, CCBA, ECBA, BA Foundation, PMI PMP |
| **Cloud Azure** | 2 | AZ-104, Azure Administrator |
| **Cloud GCP** | 2 | ACE, Cloud Professional |
| **Java/Spring** | 2 | Java OCP 17, Spring Professional |
| **Security** | 2 | CompTIA Security+, CISSP |

---

## ⚠️ ISSUES FOUND

### 1. Duplicate/Similar Certifications

**AWS:**
- `AWS_SOLUTIONS_ARCHITECT` (73 skills)
- `AWS_SAA_C03` (71 skills)
→ Có vẻ cùng 1 certification, khác naming

**Azure:**
- `AZURE_AZ104` (74 skills)
- `MICROSOFT_AZURE_ADMINISTRATOR` (71 skills)
→ Cùng certification

**GCP:**
- `GCP_ACE` (95 skills)
- `GOOGLE_CLOUD_PROFESSIONAL` (82 skills)
→ Có thể khác level (Associate vs Professional)

**ISTQB:**
- `ISTQB_CTFL` (103 skills)
- `ISTQB_FOUNDATION_V4_0` (103 skills)
→ Cùng certification, khác version?

**ISTQB Agile:**
- `ISTQB_AGILE` (28 skills)
- `ISTQB_AGILE_TESTER` (56 skills)
→ Có thể khác

**ISTQB AI:**
- `ISTQB_AI` (13 skills)
- `ISTQB_AI_TESTING` (59 skills)
→ Có thể khác

**Recommendation:** Clarify và merge duplicates hoặc mark as aliases

---

### 2. Inconsistent Naming Conventions

**Pattern 1:** Vendor_ExamCode (AWS_SAA_C03) ✅ Good
**Pattern 2:** Vendor_Description (AWS_SOLUTIONS_ARCHITECT) ❌ Inconsistent
**Pattern 3:** Domain_Skill (DEV_BACKEND) ✅ Good
**Pattern 4:** Org_Cert (ISTQB_CTFL) ✅ Good

**Recommendation:** Standardize to Pattern 1 where possible:
- `AWS_SOLUTIONS_ARCHITECT` → Keep as alias, primary: `AWS_SAA_C03`
- `MICROSOFT_AZURE_ADMINISTRATOR` → `AZURE_AZ104`

---

### 3. Missing Popular Certifications

**Security (Should Add):**
- CEH (Certified Ethical Hacker)
- OSCP (Offensive Security)
- CISM (Certified Information Security Manager)

**Cloud (Should Add):**
- AWS_SOA_C02 (SysOps Administrator)
- AWS_ANS_C01 (Advanced Networking)
- AZURE_AZ400 (DevOps Engineer)
- AZURE_DP900 (Data Fundamentals)

**Development (Should Add):**
- MongoDB Certified Developer
- Redis Certified Developer

**Agile/PM:**
- SAFe Agilist
- PRINCE2

---

### 4. Category Overlaps

**ISTQB_AGILE appears in 2 categories:**
- Agile/Scrum ✅
- Testing/QA ✅

**Solution:** Use primary + secondary category system

---

## 💡 RECOMMENDED TAXONOMY STRUCTURE

### Level 1: Domain (8 domains)
```
1. CLOUD_COMPUTING
   ├── AWS (5 certs)
   ├── Azure (2 certs)
   └── GCP (2 certs)

2. SOFTWARE_DEVELOPMENT
   ├── Backend (Python, Node, Golang, Java)
   ├── Frontend (React, JavaScript/TS)
   ├── Full Stack (API Design, System Design)
   ├── Architecture (Software Arch, DevOps)
   └── Database (SQL, Database Design)

3. QUALITY_ASSURANCE
   ├── ISTQB Foundation
   ├── ISTQB Agile
   ├── ISTQB AI
   └── Test Analyst

4. AGILE_METHODOLOGIES
   ├── Scrum Master (PSM I, II)
   ├── Product Owner (PSPO I)
   └── Agile Coach

5. INFRASTRUCTURE_DEVOPS
   ├── Containers (Docker, Kubernetes)
   ├── IaC (Terraform)
   └── CI/CD

6. SECURITY
   ├── CompTIA Security+
   ├── CISSP
   └── (Add: CEH, OSCP)

7. BUSINESS_ANALYSIS
   ├── Entry (ECBA)
   ├── Certification (CCBA)
   └── Professional (CBAP)

8. PROJECT_MANAGEMENT
   ├── PMI PMP
   └── (Add: PRINCE2, SAFe)
```

### Level 2: Career Paths (Map to certifications)
```
SCRUM_MASTER → [PSM_I, PSM_II, AGILE_SCRUM_MASTER]
PRODUCT_OWNER → [SCRUM_PSPO_I]
QA_ENGINEER → [ISTQB_CTFL, ISTQB_AGILE, ISTQB_AI]
BACKEND_DEV → [DEV_BACKEND, DEV_NODEJS, DEV_PYTHON, DEV_API_DESIGN]
FRONTEND_DEV → [DEV_FRONTEND, DEV_REACT, DEV_JAVASCRIPT_TS]
DEVOPS_ENGINEER → [DEV_DEVOPS, DOCKER_DCA, KUBERNETES_CKA, HASHICORP_TERRAFORM]
CLOUD_ARCHITECT → [AWS_SAA_C03, AZURE_AZ104, GCP_ACE]
SECURITY_ANALYST → [COMPTIA_SECURITY_PLUS, ISC2_CISSP]
BUSINESS_ANALYST → [ECBA, CCBA, CBAP, BUSINESS_ANALYST_FOUNDATION]
PROJECT_MANAGER → [PMI_PMP]
```

---

## 🔧 PROPOSED ACTIONS

### Immediate (Can do now):

#### 1. Add Category Field to Database
```sql
ALTER TABLE wp_ez_skills
ADD COLUMN primary_category VARCHAR(50),
ADD COLUMN secondary_category VARCHAR(50),
ADD INDEX idx_skills_primary_category(primary_category);

UPDATE wp_ez_skills
SET primary_category = CASE
    WHEN certification_id LIKE 'AWS_%' THEN 'CLOUD_AWS'
    WHEN certification_id LIKE 'AZURE_%' THEN 'CLOUD_AZURE'
    WHEN certification_id LIKE 'GCP_%' THEN 'CLOUD_GCP'
    WHEN certification_id LIKE '%PSM%' OR certification_id LIKE '%SCRUM%' THEN 'AGILE_SCRUM'
    WHEN certification_id LIKE 'ISTQB_%' THEN 'TESTING_QA'
    WHEN certification_id LIKE 'DEV_%' THEN 'DEVELOPMENT'
    WHEN certification_id LIKE '%KUBERNETES%' OR certification_id LIKE '%DOCKER%' THEN 'CONTAINER_INFRA'
    -- etc.
END;
```

#### 2. Add Certification Metadata Table
```sql
CREATE TABLE wp_ez_certifications (
    id INT AUTO_INCREMENT PRIMARY KEY,
    certification_id VARCHAR(100) UNIQUE NOT NULL,
    name VARCHAR(255) NOT NULL,
    name_vi VARCHAR(255),
    description TEXT,
    primary_category VARCHAR(50),
    secondary_category VARCHAR(50),
    level VARCHAR(20),  -- ENTRY, ASSOCIATE, PROFESSIONAL, EXPERT
    vendor VARCHAR(100), -- AWS, Microsoft, Scrum.org, ISTQB
    exam_code VARCHAR(50),
    is_active BOOLEAN DEFAULT TRUE,
    display_order INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

#### 3. Create Category Hierarchy Table
```sql
CREATE TABLE wp_ez_certification_categories (
    id INT AUTO_INCREMENT PRIMARY KEY,
    category_code VARCHAR(50) UNIQUE NOT NULL,
    category_name VARCHAR(100) NOT NULL,
    category_name_vi VARCHAR(100),
    parent_category_code VARCHAR(50),
    description TEXT,
    icon VARCHAR(255),
    display_order INT,
    FOREIGN KEY (parent_category_code) REFERENCES wp_ez_certification_categories(category_code)
);
```

#### 4. Update API Response to Include Categories
```java
@Data
public class CertificationResponse {
    private String certificationId;
    private String name;
    private String nameVi;

    // NEW fields
    private String primaryCategory;
    private String secondaryCategory;
    private String level;  // ENTRY, ASSOCIATE, PROFESSIONAL
    private String vendor;
    private String examCode;

    private Integer skillCount;
    private Integer questionCount;
}
```

---

## 📋 CLEANUP RECOMMENDATIONS

### Merge/Deprecate Duplicates:

**Keep as Primary → Deprecate as Alias:**
1. `AWS_SAA_C03` → `AWS_SOLUTIONS_ARCHITECT` (alias)
2. `AZURE_AZ104` → `MICROSOFT_AZURE_ADMINISTRATOR` (alias)
3. `ISTQB_FOUNDATION_V4_0` → `ISTQB_CTFL` (mark as v4.0)

### Add Missing Popular Certs:
```
SECURITY:
- CEH
- OSCP
- CISM

CLOUD:
- AWS_SOA_C02 (SysOps)
- AWS_ANS_C01 (Networking)
- AZURE_AZ400 (DevOps)
- AZURE_AI102 (AI Engineer)

DEVELOPMENT:
- MONGODB_CERTIFIED_DEV
- REDIS_CERTIFIED_ARCHITECT

AGILE:
- SAFE_AGILIST
- PRINCE2_FOUNDATION
```

---

## 🎯 IMPLEMENTATION PLAN

### Phase 1: Database Schema (1-2 hours)
- [ ] Create wp_ez_certifications table
- [ ] Create wp_ez_certification_categories table
- [ ] Migrate existing data
- [ ] Add category columns to wp_ez_skills

### Phase 2: API Updates (2-3 hours)
- [ ] Update CertificationResponse model
- [ ] Add category filtering endpoints
- [ ] Add career path to certification mapping API
- [ ] Update existing endpoints

### Phase 3: Data Cleanup (1-2 hours)
- [ ] Mark duplicates as aliases
- [ ] Standardize naming
- [ ] Add missing certifications
- [ ] Verify all mappings

### Phase 4: Frontend Updates (by frontend team)
- [ ] Group certifications by category
- [ ] Show hierarchy in UI
- [ ] Filter by career path
- [ ] Display level badges

---

## 📊 EXPECTED BENEFITS

### User Experience:
- ✅ Easier to find relevant certifications
- ✅ Clear career path guidance
- ✅ Better organization by domain

### Developer Experience:
- ✅ Consistent naming
- ✅ Clear taxonomy
- ✅ Easy to add new certs

### Business:
- ✅ Better conversion (easier discovery)
- ✅ Clearer value proposition
- ✅ Scalable structure

---

## 🚀 QUICK WIN (Can do now - 30 minutes)

### Add Category Grouping API:
```java
@GetMapping("/api/certifications/by-category")
public ResponseEntity<Map<String, List<CertificationResponse>>> getCertificationsByCategory() {
    List<CertificationResponse> all = getAllCertifications();

    Map<String, List<CertificationResponse>> grouped = all.stream()
        .collect(Collectors.groupingBy(cert -> {
            String id = cert.getCertificationId();
            if (id.startsWith("AWS_")) return "Cloud - AWS";
            if (id.startsWith("AZURE_")) return "Cloud - Azure";
            if (id.startsWith("GCP_")) return "Cloud - GCP";
            if (id.contains("PSM") || id.contains("SCRUM")) return "Agile/Scrum";
            if (id.startsWith("ISTQB_")) return "Testing/QA";
            if (id.startsWith("DEV_")) return "Development";
            // ... etc
            return "Other";
        }));

    return ResponseEntity.ok(grouped);
}
```

---

## ❓ QUESTIONS FOR PRODUCT TEAM

1. **Duplicates:** Keep both or merge?
   - AWS_SAA_C03 vs AWS_SOLUTIONS_ARCHITECT
   - ISTQB_CTFL vs ISTQB_FOUNDATION_V4_0

2. **Missing Certs:** Which ones to add first?
   - CEH, OSCP (Security)
   - AWS SysOps, Azure DevOps (Cloud)
   - SAFe, PRINCE2 (Agile/PM)

3. **Career Paths:** Approve proposed mappings?
   - SCRUM_MASTER → [PSM_I, PSM_II, ...]
   - DEVOPS_ENGINEER → [Docker, K8s, Terraform, ...]

4. **Naming:** Standardize all to exam codes?
   - Or keep descriptive names?

---

**Recommendation:**
1. Implement quick win (category grouping API) now - 30 minutes
2. Plan full taxonomy restructure for v1.4
3. Get product team input on duplicates

**Should I implement the quick win API now?**
