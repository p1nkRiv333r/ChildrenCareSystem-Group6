package com.dental.controller;

import com.dental.entity.*;
import com.dental.service.*;
import com.dental.util.Const;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.sql.Date;
import java.sql.SQLOutput;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Controller// Đánh dấu lớp này là Controller trong Spring MVC
@RequestMapping() // Xác định đường dẫn gốc cho các request
public class AppointmentController {
    // Inject các service vào controller
    @Autowired
    AppointmentService appointmentService;

    @Autowired
    PatientService patientService;

    @Autowired
    UserService userService;

    @Autowired
    SService serviceService;

    @Autowired
    RateStarService rateStarService;

    @Autowired
    DoctorService doctorService;
    /**
     * Hiển thị form đặt lịch hẹn
     */
    @GetMapping("/booking")
    public String formAppointment(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam(value = "serviceId", required = false) List<Integer> serviceIds,
            Model model
    ) {
        if (userDetails == null) {
            return "redirect:/";// Chuyển hướng về trang chủ nếu chưa đăng nhập
        }
        if (serviceIds == null) {
            serviceIds = new ArrayList<>();
        }

        model.addAttribute("appointment", new Appointment());
        model.addAttribute("user", new User());
        model.addAttribute("services", serviceService.getAll());
        model.addAttribute("selectedServices", serviceIds);
        return "landing/appointment/booking";// Trả về template booking
    }
    /**
     * Hiển thị danh sách cuộc hẹn của bệnh nhân hoặc bác sĩ
     */
/**
 * Phương thức Controller để lấy danh sách các cuộc hẹn dựa trên vai trò người dùng, ngày tháng và trạng thái.
 * Truy cập qua phương thức GET tại endpoint "/appointments".
 */
    @GetMapping("/appointments")
    public String listAppointment(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            Model model,
            @RequestParam(name = "page", required = false, defaultValue = Const.PAGE_DEFAULT_STR) Integer pageNum,
            @RequestParam(name = "pageSize", required = false, defaultValue = Const.PAGE_SIZE_DEFAULT_STR) Integer pageSize,
            @RequestParam(name = "date", required = false) Date date,
            @RequestParam(name = "status", required = false) String status
    ) {
        if (userDetails == null) {
            return "redirect:/";
        }
    }
    /**
     * Hiển thị danh sách cuộc hẹn của bệnh nhân hoặc bác sĩ
     */
/**
 * Phương thức Controller để lấy danh sách các cuộc hẹn dựa trên vai trò người dùng, ngày tháng và trạng thái.
 * Truy cập qua phương thức GET tại endpoint "/appointments".
 */
@GetMapping("/appointments")
public String listAppointment(
        @AuthenticationPrincipal UserDetailsImpl userDetails, // Lấy thông tin người dùng đã xác thực
        Model model,
        @RequestParam(name = "page", required = false, defaultValue = Const.PAGE_DEFAULT_STR) Integer pageNum, // Phân trang: số trang hiện tại
        @RequestParam(name = "pageSize", required = false, defaultValue = Const.PAGE_SIZE_DEFAULT_STR) Integer pageSize, // Phân trang: số mục trên mỗi trang
        @RequestParam(name = "date", required = false) Date date, // Bộ lọc tùy chọn: ngày hẹn
        @RequestParam(name = "status", required = false) String status // Bộ lọc tùy chọn: trạng thái cuộc hẹn
) {
    // Chuyển hướng về trang chủ nếu người dùng chưa đăng nhập
    if (userDetails == null) {
        return "redirect:/";
    }

    // Đảm bảo số trang ít nhất là 1
    if (pageNum < 1) {
        pageNum = 1;
    }

    String role = userDetails.getUserEntity().getRole(); // Lấy vai trò người dùng (Patient/Doctor)
    int userId = userDetails.getUserEntity().getUserId(); // Lấy ID của người dùng
    Pageable pageable = PageRequest.of(pageNum - 1, pageSize); // Tạo yêu cầu phân trang
    Page<Appointment> appointments;
    
    // Xác định truy vấn dựa trên vai trò của người dùng
    if (role.equals("Patient")) {
        // Nếu người dùng là bệnh nhân, lấy danh sách cuộc hẹn dựa trên bộ lọc
        if (date != null && status != null && !status.isEmpty()) {
            appointments = appointmentService.findAllByPatientPatientIdAndStatusAndDate(userId, status, date, pageable);
        } else if (status != null && !status.isEmpty()) {
            appointments = appointmentService.findAllByPatientPatientIdAndStatus(userId, status, pageable);
        } else if (date != null) {
            appointments = appointmentService.findAllByPatientPatientIdAndDate(userId, date, pageable);
        } else {
            appointments = appointmentService.findAllByPatientPatientIdOrderByDateDesc(userId, pageable);
        }
    } else {
        // Nếu người dùng là bác sĩ, lấy danh sách cuộc hẹn tương ứng
        if (date != null && status != null && !status.isEmpty()) {
            appointments = appointmentService.findAllByDoctorDoctorIdAndStatusAndDate(userId, status, date, pageable);
        } else if (status != null && !status.isEmpty()) {
            appointments = appointmentService.findAllByDoctorDoctorIdAndStatus(userId, status, pageable);
        } else if (date != null) {
            appointments = appointmentService.findAllByDoctorDoctorIdAndDate(userId, date, pageable);
        } else {
            appointments = appointmentService.findAllByDoctorDoctorIdOrderByDateDesc(userId, pageable);
        }
    }

    // Thêm dữ liệu vào model để hiển thị trên giao diện
    model.addAttribute("user", userService.get(userDetails.getUserEntity().getUserId())); // Thông tin người dùng
    model.addAttribute("appointments", appointments); // Danh sách cuộc hẹn
    model.addAttribute("status", status); // Bộ lọc trạng thái
    model.addAttribute("date", date); // Bộ lọc ngày hẹn
    model.addAttribute("numberOfPage", appointments.getTotalPages()); // Tổng số trang để phân trang
    
    return "landing/appointment/appointments"; // Trả về template giao diện hiển thị danh sách cuộc hẹn
}
    /**
     * Hiển thị chi tiết một cuộc hẹn
     */
/**
 * Phương thức Controller để lấy chi tiết cuộc hẹn theo ID.
 * Truy cập qua phương thức GET tại endpoint "/appointment/{appointmentId}".
 */
@GetMapping("/appointment/{appointmentId}")
public String appointmentDetail(
        @PathVariable("appointmentId") int appointmentId, // ID cuộc hẹn từ URL
        Model model,
        @AuthenticationPrincipal UserDetailsImpl userDetails // Lấy thông tin người dùng đã xác thực
) {
    // Nếu người dùng chưa đăng nhập, chuyển hướng về trang chủ
    if (userDetails == null) {
        return "redirect:/";
    }

    String role = userDetails.getUserEntity().getRole(); // Lấy vai trò người dùng
    int userId = userDetails.getUserEntity().getUserId(); // Lấy ID của người dùng
    Optional<Appointment> a = appointmentService.getById(appointmentId); // Tìm cuộc hẹn theo ID

    // Nếu cuộc hẹn không tồn tại, chuyển hướng về danh sách cuộc hẹn
    if (a == null || a.isEmpty()) {
        return "redirect:/appointments";
    }
    Appointment appointment = a.get(); // Lấy đối tượng cuộc hẹn
    List<Service> services = appointment.getService(); // Lấy danh sách dịch vụ của cuộc hẹn
    double totalPrice = 0; // Khởi tạo tổng giá trị dịch vụ
    for (Service s : services) {
        totalPrice += Double.parseDouble(s.getPrice()); // Tính tổng giá dịch vụ
    }

    // Kiểm tra quyền truy cập vào cuộc hẹn dựa trên vai trò
    if (role.equals("Patient")) {
        // Nếu là bệnh nhân, đảm bảo cuộc hẹn thuộc về họ
        if (appointment.getPatient().getPatientId() != userId) {
            return "redirect:/appointments";
        }
    } else {
        // Nếu là bác sĩ, đảm bảo cuộc hẹn có bác sĩ và thuộc về họ
        if (appointment.getDoctor() == null) {
            return "redirect:/appointments";
        }
        if (appointment.getDoctor().getDoctorId() != userId) {
            return "redirect:/appointments";
        }
    }

    // Thêm dữ liệu vào model để hiển thị trên giao diện
    model.addAttribute("user", userService.get(userDetails.getUserEntity().getUserId())); // Thông tin người dùng
    model.addAttribute("appointment", appointment); // Chi tiết cuộc hẹn
    model.addAttribute("rateStar", new RateStar()); // Đối tượng đánh giá sao
    model.addAttribute("totalPrice", totalPrice); // Tổng giá trị dịch vụ
    
    return "landing/appointment/appointment-detail"; // Trả về trang chi tiết cuộc hẹn
}

    /**
     * Xử lý đặt lịch hẹn mới
     */
    @PostMapping("/booking/save")
    public String makeAppointment(
            @Valid Appointment appointment,
            BindingResult result,
            @Valid User user,
            BindingResult userResult,
            Model model,
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam(value = "services", required = false) List<Integer> serviceIds
    ) {
        // Kiểm tra nếu người dùng chưa đăng nhập, chuyển hướng về trang chủ
        if (userDetails == null) {
            return "redirect:/";
        }
    
        String role = userDetails.getUserEntity().getRole();
        boolean hasErr = false;
    
        // Chỉ bệnh nhân mới có thể đặt lịch hẹn
        if (!role.equals("Patient")) {
            hasErr = true;
            model.addAttribute("errMes", "Only patient can make appointment");
        }
    
        // Kiểm tra nếu không có dịch vụ nào được chọn
        if (serviceIds == null) {
            hasErr = true;
            model.addAttribute("service", "Please choose at least 1 service");
        }
    
        // Giới hạn số dịch vụ tối đa có thể chọn là 3
        if (serviceIds != null && serviceIds.size() > 3) {
            hasErr = true;
            model.addAttribute("service", "Please choose max 3 services");
        }
    
        // Kiểm tra lỗi đầu vào
        if (result.hasErrors() || userResult.hasFieldErrors("phoneNumber") || hasErr) {
            model.addAttribute("services", serviceService.getAll());
            model.addAttribute("selectedServices", serviceIds);
            return "landing/appointment/booking";
        }
    
        // Kiểm tra ngày đặt lịch
        LocalDate currentDate = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate comparisonDate = LocalDate.parse(appointment.getDateString(), formatter);
    
        // Kiểm tra nếu đặt lịch vào ngày hiện tại
        if (comparisonDate.equals(currentDate)) {
            ZoneId zone = ZoneId.of("Asia/Ho_Chi_Minh");
            LocalTime currentTime = LocalTime.now(zone);
            DateTimeFormatter formatter2 = DateTimeFormatter.ofPattern("HH");
            String hour24Format = currentTime.format(formatter2);
    
            // Nếu đặt buổi sáng, kiểm tra giờ hiện tại có vượt quá 9h không
            if (appointment.getTime().equals("Morning") && Integer.parseInt(hour24Format) >= 9) {
                model.addAttribute("services", serviceService.getAll());
                model.addAttribute("selectedServices", serviceIds);
                model.addAttribute("time", "Booking time for this morning is over");
                return "landing/appointment/booking";
            }
    
            // Nếu đặt buổi chiều, kiểm tra giờ hiện tại có vượt quá 15h không
            if (appointment.getTime().equals("Afternoon") && Integer.parseInt(hour24Format) >= 15) {
                model.addAttribute("services", serviceService.getAll());
                model.addAttribute("selectedServices", serviceIds);
                model.addAttribute("time", "Booking time for this day is over");
                return "landing/appointment/booking";
            }
        }
    
        // Kiểm tra số lượng bác sĩ khả dụng so với số lượng lịch hẹn
        long numOfDoctors = doctorService.countDoctorsByUserStatus();
        int numOfAppointment = appointmentService.countAppointmentsByDateAndTime(appointment.getDate(), appointment.getTime());
        if (numOfAppointment >= numOfDoctors * 5) {
            model.addAttribute("errMes", "This slot is full. You can book another slot or day!");
            model.addAttribute("services", serviceService.getAll());
            model.addAttribute("selectedServices", serviceIds);
            return "landing/appointment/booking";
        }
    
        // Lấy danh sách dịch vụ đã chọn
        List<Service> selectedServices = serviceService.getAllByIds(serviceIds);
        int patientId = userDetails.getUserEntity().getUserId();
        Patient patient = patientService.get(patientId);
        
        // Thiết lập thông tin cho cuộc hẹn
        appointment.setPatient(patient);
        appointment.setStatus("New");
        appointment.setService(selectedServices);
    
        try {
            // Lưu cuộc hẹn vào cơ sở dữ liệu
            appointmentService.save(appointment);
            return "redirect:/appointments";
        } catch (Error e) {
            // Nếu có lỗi xảy ra, quay lại trang đặt lịch
            return "landing/appointment/booking";
        }
    }
    
    /**
     * Xử lý đặt lịch hẹn mới
     */
    @PostMapping("/appointments/delete/{appointmentId}")
    public String cancelAppointment(@PathVariable("appointmentId") int appointmentId, Model model) throws IllegalAccessException {
        try {
            Appointment p = appointmentService.get(appointmentId);
            if (p != null && p.getStatus().equals("Completed")) {
                throw new IllegalAccessException("Cannot cancel this appointment!");
            }

            if (p.getStatus().equals("Cancel")) {
                throw new IllegalAccessException("Cannot cancel this appointment!");
            }

            appointmentService.cancelAppointment(appointmentId);
        } catch (Error e) {
            throw new IllegalAccessException("Failed to cancel!");
        }
        return "redirect:/appointments";
    }

    @PostMapping("/appoinment/completed/{appointmentId}")
public String completeAppointment(@PathVariable("appointmentId") int appointmentId, Model model) throws IllegalAccessException {
    try {
        // Lấy thông tin cuộc hẹn theo ID
        Appointment p = appointmentService.get(appointmentId);
        
        // Kiểm tra trạng thái của cuộc hẹn, chỉ cho phép hoàn thành nếu trạng thái là "Assigned"
        if (p != null && !p.getStatus().equals("Assigned")) {
            throw new IllegalAccessException("Cannot change this appointment to complete !");
        }
        
        // Cập nhật trạng thái cuộc hẹn thành "Completed"
        appointmentService.updateAppointmentStatus("Completed", appointmentId);
    } catch (Error e) {
        throw new IllegalAccessException("Failed to change status!");
    }
    
    // Chuyển hướng về trang chi tiết cuộc hẹn
    return "redirect:/appointment/" + appointmentId;
}

@GetMapping("/admin/appointments")
public String viewListAppointment(Model model,
                                  @AuthenticationPrincipal UserDetailsImpl userDetails,
                                  @RequestParam(name = "page", required = false, defaultValue = Const.PAGE_DEFAULT_STR) Integer pageNum,
                                  @RequestParam(name = "pageSize", required = false, defaultValue = Const.PAGE_SIZE_DEFAULT_STR) Integer pageSize,
                                  @RequestParam(name = "fullName", required = false) String fullName,
                                  @RequestParam(name = "date", required = false) Date date,
                                  @RequestParam(name = "status", required = false) String status) {

    System.out.println("==============Test==========:" + "page=" + pageNum + " pageSize=" + pageSize + " date=" + date + " status=" + status + " fullName=" + fullName);
    
    // Kiểm tra nếu người dùng chưa đăng nhập, chuyển hướng về trang chủ
    if (userDetails == null) {
        return "redirect:/";
    }
    
    // Đảm bảo số trang hợp lệ
    if (pageNum < 1) {
        pageNum = 1;
    }
    
    // Tạo đối tượng phân trang
    Pageable pageable = PageRequest.of(pageNum - 1, pageSize);
    Page<Appointment> appointments = appointmentService.findAllByOrderByDateDesc(pageable);
    List<Doctor> doctors = doctorService.getAll();
    
    // Lọc danh sách cuộc hẹn dựa trên các tham số tìm kiếm
    if (fullName != null && !fullName.isEmpty() && status != null && !status.isEmpty() && date != null) {
        appointments = appointmentService.findAllByStatusAndDateAndPatientUserFullName(status, date, fullName, pageable);
    } else if (date != null && fullName != null && !fullName.isEmpty()) {
        appointments = appointmentService.findAllByDateAndPatientUserFullName(date, fullName, pageable);
    } else if (date != null && status != null && !status.isEmpty()) {
        appointments = appointmentService.findAllByStatusAndDate(status, date, pageable);
    } else if (fullName != null && !fullName.isEmpty() && status != null && !status.isEmpty()) {
        appointments = appointmentService.findAllByStatusAndPatientUserFullName(status, fullName, pageable);
    } else if (status != null && !status.isEmpty()) {
        appointments = appointmentService.findAllByStatus(status, pageable);
    } else if (fullName != null && !fullName.isEmpty()) {
        appointments = appointmentService.findAllByPatientUserFullName(fullName, pageable);
    } else if (date != null) {
        appointments = appointmentService.findAllByDate(date, pageable);
    }
    
    // Thêm dữ liệu vào model để hiển thị trên giao diện
    model.addAttribute("appointments", appointments);
    model.addAttribute("doctors", doctors);
    model.addAttribute("status", status);
    model.addAttribute("date", date);
    model.addAttribute("user", userService.get(userDetails.getUserEntity().getUserId()));
    model.addAttribute("numberOfPage", appointments.getTotalPages());
    model.addAttribute("fullName", fullName);
    
    return "admin/appointment/appointment";
}

@PostMapping("/admin/assign-appointment")
public String assignDoctor(@RequestParam("doctorId") int doctorId,
                           @RequestParam("appointmentId") int appointmentId,
                           RedirectAttributes redirectAttributes) {
    
    // Lấy thông tin bác sĩ và cuộc hẹn
    Doctor doctor = doctorService.get(doctorId);
    Optional<Appointment> a = appointmentService.getById(appointmentId);
    if (a.isEmpty()) {
        return "redirect:/admin/appointments";
    }
    Appointment appointment = a.get();
    
    // Kiểm tra trạng thái cuộc hẹn, nếu đã bị hủy hoặc hoàn thành thì không thể gán bác sĩ
    if (appointment.getStatus().equals("Cancel") || appointment.getStatus().equals("Completed")) {
        redirectAttributes.addFlashAttribute("errorMessage", "Cannot assign");
        return "redirect:/admin/appointment/" + appointmentId;
    }
    
    // Kiểm tra nếu cuộc hẹn đã qua ngày hiện tại
    LocalDate currentDate = LocalDate.now();
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    LocalDate appointmentDate = LocalDate.parse(appointment.getDateString(), formatter);
    
    if (appointmentDate.isBefore(currentDate)) {
        redirectAttributes.addFlashAttribute("errorMessage", "Overtime to assign");
        return "redirect:/admin/appointment/" + appointmentId;
    }
    
    // Kiểm tra nếu cuộc hẹn là trong ngày và thời gian đã trôi qua
    if (appointmentDate.equals(currentDate)) {
        ZoneId zone = ZoneId.of("Asia/Ho_Chi_Minh");
        LocalTime currentTime = LocalTime.now(zone);
        
        if (appointment.getTime().equals("Morning") && currentTime.getHour() >= 12) {
            redirectAttributes.addFlashAttribute("errorMessage", "Overtime to assign");
            return "redirect:/admin/appointment/" + appointmentId;
        }
        if (appointment.getTime().equals("Afternoon") && currentTime.getHour() >= 18) {
            redirectAttributes.addFlashAttribute("errorMessage", "Overtime to assign");
            return "redirect:/admin/appointment/" + appointmentId;
        }
    }
    
    // Gán bác sĩ cho cuộc hẹn và cập nhật trạng thái
    appointment.setDoctor(doctor);
    appointmentService.updateAppointmentDoctor(doctor.getDoctorId(), appointmentId);
    appointmentService.updateAppointmentStatus("Assigned", appointmentId);
    
    return "redirect:/admin/appointment/" + appointmentId;
}

    /**
     * Đánh giá và phản hồi dịch vụ
     */
    @PostMapping("/feedback/save")
    public String feedback(
            @RequestParam("serviceId") int serviceId,
            @RequestParam("star") int star,
            @RequestParam("feedback") String feedback,
            Model model,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) throws Exception {
        int userId = userDetails.getUserEntity().getUserId();
        String role = userDetails.getUserEntity().getRole();

        if (!role.equals("Patient")) {
            throw new IllegalAccessException("You cannot send feedback");
        }

        int count = appointmentService.findCountByPatientIdAndServiceId(userId, serviceId);
        int countS = rateStarService.countAllByUserUserIdAndServiceServiceId(userId, serviceId);

        if (countS >= count) {
            throw new Exception("You just can feedback 1 times/service");
        }

        RateStar rateStar = new RateStar(star, feedback, userDetails.getUserEntity(), serviceService.get(serviceId));

        rateStarService.saveRateStar(rateStar);
        return "redirect:/service/" + serviceId;
    }
}
