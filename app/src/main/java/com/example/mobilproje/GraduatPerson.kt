import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import java.io.Serializable
import java.util.Date



data class GraduatPerson(
    var name :String = "",
    var surName: String = "",
    var email: String = "",
    var phoneNumber: String? = null,
    var startDate: String = "",
    var endDate: String = "",
    var userName: String = "",
    var password: String = "",
    var photo: String? = null
) : Serializable

data class RequestDataClass(
    var senderUserName: String = "",
    var recieverUserName: String = "",
    var isOkey: Boolean = false,
    var senderName: String = ""
) : Serializable
data class MyLocation(var latitude: Double, var longitude: Double, var userName: String, var name: String, var surName: String) {
    constructor() : this(0.0, 0.0, "","","")
}
