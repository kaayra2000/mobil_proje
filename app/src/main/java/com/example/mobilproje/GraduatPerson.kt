import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import java.io.Serializable
import java.util.Date

enum class situation{
    Licence,
    Degree,
    Doctorate
}

data class GraduatPerson(
    var name :String,
    var surName: String,
    var email: String,
    var phoneNumber: String?,
    var startDate: String,
    var endDate: String,
    var situation: situation,
    var userName: String,
    var password: String,
    var photo: String?
) : Serializable
