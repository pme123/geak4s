package pme123.geak4s.validation

/** Validation result */
sealed trait ValidationResult:
  def isValid: Boolean = this match
    case ValidationResult.Valid => true
    case ValidationResult.Invalid(_) => false
  
  def errorMessage: Option[String] = this match
    case ValidationResult.Valid => None
    case ValidationResult.Invalid(msg) => Some(msg)

object ValidationResult:
  case object Valid extends ValidationResult
  case class Invalid(message: String) extends ValidationResult

/** Validation rules for form fields */
object Validators:
  
  /** Validate required field */
  def required(value: String): ValidationResult =
    if value.trim.nonEmpty then ValidationResult.Valid
    else ValidationResult.Invalid("This field is required")
  
  /** Validate email format */
  def email(value: String): ValidationResult =
    if value.isEmpty then ValidationResult.Valid // Empty is valid, use required() for mandatory
    else if value.matches("""^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$""") then
      ValidationResult.Valid
    else
      ValidationResult.Invalid("Please enter a valid email address")
  
  /** Validate phone number (Swiss format) */
  def phone(value: String): ValidationResult =
    if value.isEmpty then ValidationResult.Valid
    else if value.matches("""^(\+41|0041|0)?[\s]?(\d{2})[\s]?(\d{3})[\s]?(\d{2})[\s]?(\d{2})$""") then
      ValidationResult.Valid
    else
      ValidationResult.Invalid("Please enter a valid phone number (e.g., +41 44 123 45 67)")
  
  /** Validate Swiss ZIP code (4 digits) */
  def swissZip(value: String): ValidationResult =
    if value.isEmpty then ValidationResult.Valid
    else if value.matches("""^\d{4}$""") then
      ValidationResult.Valid
    else
      ValidationResult.Invalid("Please enter a valid 4-digit ZIP code")
  
  /** Validate number (integer) */
  def integer(value: String): ValidationResult =
    if value.isEmpty then ValidationResult.Valid
    else if value.toIntOption.isDefined then
      ValidationResult.Valid
    else
      ValidationResult.Invalid("Please enter a valid integer number")
  
  /** Validate number (decimal) */
  def decimal(value: String): ValidationResult =
    if value.isEmpty then ValidationResult.Valid
    else if value.toDoubleOption.isDefined then
      ValidationResult.Valid
    else
      ValidationResult.Invalid("Please enter a valid decimal number")
  
  /** Validate positive number */
  def positive(value: String): ValidationResult =
    if value.isEmpty then ValidationResult.Valid
    else value.toDoubleOption match
      case Some(num) if num > 0 => ValidationResult.Valid
      case Some(_) => ValidationResult.Invalid("Please enter a positive number")
      case None => ValidationResult.Invalid("Please enter a valid number")
  
  /** Validate non-negative number (>= 0) */
  def nonNegative(value: String): ValidationResult =
    if value.isEmpty then ValidationResult.Valid
    else value.toDoubleOption match
      case Some(num) if num >= 0 => ValidationResult.Valid
      case Some(_) => ValidationResult.Invalid("Please enter a non-negative number")
      case None => ValidationResult.Invalid("Please enter a valid number")
  
  /** Validate number within range */
  def range(min: Double, max: Double)(value: String): ValidationResult =
    if value.isEmpty then ValidationResult.Valid
    else value.toDoubleOption match
      case Some(num) if num >= min && num <= max => ValidationResult.Valid
      case Some(_) => ValidationResult.Invalid(s"Please enter a number between $min and $max")
      case None => ValidationResult.Invalid("Please enter a valid number")
  
  /** Validate minimum length */
  def minLength(length: Int)(value: String): ValidationResult =
    if value.length >= length then ValidationResult.Valid
    else ValidationResult.Invalid(s"Please enter at least $length characters")
  
  /** Validate maximum length */
  def maxLength(length: Int)(value: String): ValidationResult =
    if value.length <= length then ValidationResult.Valid
    else ValidationResult.Invalid(s"Please enter at most $length characters")
  
  /** Validate pattern (regex) */
  def pattern(regex: String, errorMsg: String)(value: String): ValidationResult =
    if value.isEmpty then ValidationResult.Valid
    else if value.matches(regex) then
      ValidationResult.Valid
    else
      ValidationResult.Invalid(errorMsg)
  
  /** Validate URL */
  def url(value: String): ValidationResult =
    if value.isEmpty then ValidationResult.Valid
    else if value.matches("""^https?://[^\s/$.?#].[^\s]*$""") then
      ValidationResult.Valid
    else
      ValidationResult.Invalid("Please enter a valid URL (e.g., https://example.com)")
  
  /** Validate date format (YYYY-MM-DD) */
  def dateFormat(value: String): ValidationResult =
    if value.isEmpty then ValidationResult.Valid
    else if value.matches("""^\d{4}-\d{2}-\d{2}$""") then
      ValidationResult.Valid
    else
      ValidationResult.Invalid("Please enter a valid date (YYYY-MM-DD)")
  
  /** Combine multiple validators (all must pass) */
  def combine(validators: (String => ValidationResult)*)(value: String): ValidationResult =
    validators.foldLeft[ValidationResult](ValidationResult.Valid) { (acc, validator) =>
      acc match
        case ValidationResult.Valid => validator(value)
        case invalid => invalid
    }

end Validators

/** Field validation state */
case class FieldValidation(
  value: String,
  result: ValidationResult,
  touched: Boolean = false
):
  def isValid: Boolean = result.isValid
  def showError: Boolean = touched && !isValid
  def errorMessage: Option[String] = if showError then result.errorMessage else None

object FieldValidation:
  def empty: FieldValidation = FieldValidation("", ValidationResult.Valid, touched = false)

