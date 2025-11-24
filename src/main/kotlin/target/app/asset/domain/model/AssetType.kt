package target.app.asset.domain.model

enum class AssetType(val code: String, val typeGroup: String) {
  CASH("cash", "cash"),
  CHECKING("checking", "cash"),
  SAVINGS("savings", "cash"),
  FUNDS("funds", "investment"),
  STOCKS("stocks", "investment"),
  RSU("RSU", "investment"),
  PENSION("pension", "investment"),
}
