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

