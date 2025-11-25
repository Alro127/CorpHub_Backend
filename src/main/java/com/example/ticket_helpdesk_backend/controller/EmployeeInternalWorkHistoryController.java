package com.example.ticket_helpdesk_backend.controller;

public class EmployeeInternalWorkHistoryController {

    // Về phần thêm thì sẽ chỉ cần tạo service, khi gọi API chuyển phòng thăng chức thì sẽ đồng thời gọi đến service này
//    Thêm (khi chuyển phòng / thăng chức)	POST	/api/employees/{id}/internal-work-history
    // Về phần lấy danh sách này, không nên tạo API riêng như vậy, chắc là cũng chỉ nên lấy service thôi là đủ
//    Danh sách	GET	/api/employees/{id}/internal-work-history
}
