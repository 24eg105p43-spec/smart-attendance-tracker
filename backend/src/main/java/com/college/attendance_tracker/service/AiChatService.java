package com.college.attendance_tracker.service;

import com.college.attendance_tracker.dto.AttendanceSummary;
import com.college.attendance_tracker.dto.ChatResponse;
import com.college.attendance_tracker.dto.SemesterSummaryResponse;
import com.college.attendance_tracker.dto.SubjectProjection;
import com.college.attendance_tracker.model.Subject;
import com.college.attendance_tracker.repository.SubjectRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AiChatService {

    private final SubjectRepository subjectRepository;
    private final AttendanceService attendanceService;
    private final SemesterService semesterService;

    public AiChatService(SubjectRepository subjectRepository,
                         AttendanceService attendanceService,
                         SemesterService semesterService) {
        this.subjectRepository = subjectRepository;
        this.attendanceService = attendanceService;
        this.semesterService = semesterService;
    }

    public ChatResponse processQuery(String message, String userEmail) {
        String query = message == null ? "" : message.toLowerCase().trim();

        List<Subject> subjects = subjectRepository.findByOwnerEmail(userEmail);
        SemesterSummaryResponse semesterSummary = semesterService.getSemesterSummary(userEmail);

        if (subjects.isEmpty()) {
            return new ChatResponse(
                    "You don't have any subjects registered yet. Add your courses on the dashboard to get personalized attendance advice and bunk predictions!",
                    List.of("Add your first subject", "Check academic calendar")
            );
        }

        List<AttendanceSummary> summaries = subjects.stream()
                .map(s -> attendanceService.getSummary(s.getId(), userEmail))
                .collect(Collectors.toList());

        List<AttendanceSummary> lowSubjects = summaries.stream()
                .filter(s -> "LOW".equalsIgnoreCase(s.status()))
                .collect(Collectors.toList());

        List<AttendanceSummary> safeSubjects = summaries.stream()
                .filter(s -> "SAFE".equalsIgnoreCase(s.status()))
                .collect(Collectors.toList());

        List<String> suggestedActions = new ArrayList<>();
        StringBuilder reply = new StringBuilder();

        // 1. "Can I bunk / take leave / skip class?"
        if (query.contains("bunk") || query.contains("leave") || query.contains("skip") || query.contains("miss")) {
            if (lowSubjects.isEmpty()) {
                int minSkip = safeSubjects.stream().mapToInt(AttendanceSummary::canSkip).min().orElse(0);
                reply.append("Overall your attendance is in good standing! ");
                if (minSkip > 0) {
                    reply.append("Across all your safe subjects, you have at least ")
                            .append(minSkip)
                            .append(" buffer class(es) to skip. ");
                } else {
                    reply.append("However, some of your subjects have 0 safe bunks remaining, so any absence will put you in the danger zone. ");
                }
                reply.append("\n\nSubject breakdown:\n");
                for (AttendanceSummary s : summaries) {
                    reply.append("• ").append(s.subject()).append(": ")
                            .append(s.canSkip()).append(" class(es) can be safely skipped.\n");
                }
                suggestedActions.add("Show lowest attendance subject");
                suggestedActions.add("Semester projection");
            } else {
                reply.append("⚠️ Caution advised! You currently have ")
                        .append(lowSubjects.size())
                        .append(" subject(s) below the required criteria:\n");
                for (AttendanceSummary s : lowSubjects) {
                    reply.append("• ").append(s.subject()).append(" is at ").append(s.percentage())
                            .append("% (Need to attend ").append(s.needToAttend()).append(" consecutive classes).\n");
                }
                reply.append("\nAvoid skipping any lectures in these subjects!");
                suggestedActions.add("Recovery plan for low subjects");
            }
        }
        // 2. "Status / Summary / Overview"
        else if (query.contains("status") || query.contains("summary") || query.contains("report") || query.contains("overview")) {
            double avgPercentage = summaries.stream().mapToDouble(AttendanceSummary::percentage).average().orElse(0.0);
            reply.append("📊 Here is your College Attendance Summary:\n")
                    .append("• Average Attendance: ").append(String.format("%.2f", avgPercentage)).append("%\n")
                    .append("• Safe Subjects: ").append(safeSubjects.size()).append(" / ").append(summaries.size()).append("\n")
                    .append("• At-Risk Subjects: ").append(lowSubjects.size()).append("\n")
                    .append("• Remaining Class Days in Semester: ").append(semesterSummary.totalRemainingClassDays()).append("\n");

            if (!lowSubjects.isEmpty()) {
                reply.append("\n⚠️ Urgent Attention Needed:\n");
                for (AttendanceSummary s : lowSubjects) {
                    reply.append("• ").append(s.subject()).append(": ").append(s.percentage())
                            .append("% (Must attend ").append(s.needToAttend()).append(" classes to reach 75%)\n");
                }
            } else {
                reply.append("\n🎉 Great job! All your subjects are currently meeting or exceeding criteria.");
            }
            suggestedActions.add("Can I bunk tomorrow?");
            suggestedActions.add("Semester forecast");
        }
        // 3. "Semester / Forecast / Prediction / Future"
        else if (query.contains("forecast") || query.contains("semester") || query.contains("future") || query.contains("projection")) {
            reply.append("🔮 Semester Projection (Semester ends: ").append(semesterSummary.semesterEndDate()).append("):\n")
                    .append("• Remaining Class Days: ").append(semesterSummary.totalRemainingClassDays()).append(" days\n")
                    .append("• Scheduled Holidays/Leaves: ").append(semesterSummary.totalHolidays()).append("\n\n");

            for (SubjectProjection p : semesterSummary.projections()) {
                reply.append("• ").append(p.subjectName()).append(":\n")
                        .append("   Max possible if attended every day: ").append(p.maxPossiblePercentage()).append("%\n")
                        .append("   Safe bunks left this semester: ").append(p.safeBunksAllowed()).append("\n");
            }
            suggestedActions.add("How to recover low subjects?");
            suggestedActions.add("Download CSV Report");
        }
        // 4. Default intelligent assistant reply
        else {
            reply.append("Hello! I am your Smart Attendance AI Advisor. I monitor your daily attendance and calculate exact bunk limits.\n\n")
                    .append("You currently have ").append(subjects.size()).append(" subjects tracked. ");
            if (!lowSubjects.isEmpty()) {
                reply.append("Attention: ").append(lowSubjects.size())
                        .append(" subject(s) are below the 75% threshold!");
            } else {
                reply.append("All subjects are currently in the SAFE zone.");
            }
            reply.append("\n\nAsk me questions like:\n")
                    .append("• \"Can I bunk tomorrow?\"\n")
                    .append("• \"Show my attendance status\"\n")
                    .append("• \"What is my semester forecast?\"");
            suggestedActions.add("Can I bunk tomorrow?");
            suggestedActions.add("Show my attendance status");
            suggestedActions.add("What is my semester forecast?");
        }

        return new ChatResponse(reply.toString(), suggestedActions);
    }
}
