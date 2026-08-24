# Demo data for Rails Mass Assignment Companion — used with
# `./gradlew runIde` to capture the real Marketplace screenshot. Open
# this file, the warning should appear on the permit! line.
class UsersController < ApplicationController
  def create
    User.create(unsafe_params)
  end

  private

  def unsafe_params
    # Allows ANY attribute, including role/admin -- FLAGGED.
    params.permit!
  end

  def safe_params
    # Explicit whitelist -- NOT flagged.
    params.permit(:name, :email)
  end
end
