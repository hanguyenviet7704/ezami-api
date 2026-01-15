package com.hth.udecareer.eil.enums;

import com.hth.udecareer.eil.util.EnumLocalizationHelper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.List;

/**
 * Career domains with associated certifications.
 * Supports i18n via Accept-Language header.
 */
@Getter
@RequiredArgsConstructor
public enum CareerDomain {
    SCRUM_MASTER(
            "SCRUM_MASTER",
            "Scrum Master",
            "Scrum Master",
            "Lead Scrum teams, facilitate agile ceremonies, and remove impediments",
            "Dẫn dắt đội Scrum, hỗ trợ các buổi họp agile và loại bỏ trở ngại",
            Arrays.asList("PSM_I", "SCRUM_PSM_II")
    ),
    PRODUCT_OWNER(
            "PRODUCT_OWNER",
            "Product Owner",
            "Product Owner",
            "Manage product backlog, define user stories, and maximize product value",
            "Quản lý product backlog, định nghĩa user stories và tối đa hóa giá trị sản phẩm",
            Arrays.asList("SCRUM_PSPO_I")
    ),
    DEVELOPER(
            "DEVELOPER",
            "Developer",
            "Lập trình viên",
            "Build software applications using various programming languages and frameworks",
            "Xây dựng ứng dụng phần mềm sử dụng các ngôn ngữ lập trình và framework",
            Arrays.asList("DEV_BACKEND", "DEV_FRONTEND", "JAVA_OCP_17")
    ),
    QA_ENGINEER(
            "QA_ENGINEER",
            "QA Engineer",
            "Kỹ sư QA",
            "Ensure software quality through testing, automation, and quality processes",
            "Đảm bảo chất lượng phần mềm thông qua kiểm thử, tự động hóa và quy trình chất lượng",
            Arrays.asList("ISTQB_CTFL", "ISTQB_AGILE", "ISTQB_AI")
    ),
    BUSINESS_ANALYST(
            "BUSINESS_ANALYST",
            "Business Analyst",
            "Phân tích nghiệp vụ",
            "Analyze business requirements and translate them into technical solutions",
            "Phân tích yêu cầu nghiệp vụ và chuyển đổi thành giải pháp kỹ thuật",
            Arrays.asList("CBAP", "CCBA", "ECBA")
    ),
    AGILE_COACH(
            "AGILE_COACH",
            "Agile Coach",
            "Huấn luyện viên Agile",
            "Guide organizations in agile transformation and continuous improvement",
            "Hướng dẫn tổ chức trong chuyển đổi agile và cải tiến liên tục",
            Arrays.asList("PSM_I", "SCRUM_PSPO_I")
    ),
    PROJECT_MANAGER(
            "PROJECT_MANAGER",
            "Project Manager",
            "Quản lý dự án",
            "Plan, execute, and deliver projects on time and within budget",
            "Lập kế hoạch, thực thi và giao dự án đúng thời hạn và ngân sách",
            Arrays.asList("PMI_PMP")
    ),
    DEVOPS(
            "DEVOPS",
            "DevOps Engineer",
            "Kỹ sư DevOps",
            "Automate infrastructure, CI/CD pipelines, and deployment processes",
            "Tự động hóa hạ tầng, CI/CD pipelines và quy trình triển khai",
            Arrays.asList("DEV_DEVOPS", "DOCKER_DCA", "KUBERNETES_CKA")
    ),
    CLOUD(
            "CLOUD",
            "Cloud Engineer",
            "Kỹ sư Cloud",
            "Design, deploy, and manage cloud infrastructure and services",
            "Thiết kế, triển khai và quản lý hạ tầng và dịch vụ cloud",
            Arrays.asList("AWS_SAA_C03", "AWS_DVA_C02", "AZURE_AZ104", "GCP_ACE")
    ),
    SECURITY(
            "SECURITY",
            "Security Engineer",
            "Kỹ sư bảo mật",
            "Protect systems and data through security best practices and tools",
            "Bảo vệ hệ thống và dữ liệu thông qua các phương pháp và công cụ bảo mật",
            Arrays.asList("COMPTIA_SECURITY_PLUS", "ISC2_CISSP")
    );

    private final String code;
    private final String nameEn;
    private final String nameVi;
    private final String descriptionEn;
    private final String descriptionVi;
    private final List<String> certificationCodes;

    /**
     * Get localized name based on Accept-Language header.
     * @return Localized career domain name
     */
    public String getLocalizedName() {
        return EnumLocalizationHelper.getLocalizedValue(nameEn, nameVi);
    }

    /**
     * Get localized description based on Accept-Language header.
     * @return Localized career domain description
     */
    public String getLocalizedDescription() {
        return EnumLocalizationHelper.getLocalizedValue(descriptionEn, descriptionVi);
    }

    /**
     * Find career domain by code.
     * @param code Career domain code
     * @return CareerDomain enum or null if not found
     */
    public static CareerDomain fromCode(String code) {
        if (code == null) {
            return null;
        }
        for (CareerDomain domain : values()) {
            if (domain.code.equalsIgnoreCase(code)) {
                return domain;
            }
        }
        return null;
    }
}
