class ApplicationController < ActionController::Base
  # Prevent CSRF attacks by raising an exception.
  # For APIs, you may want to use :null_session instead.
  protect_from_forgery with: :exception
  # before_action :login
  before_action :authenticate_user!
  # helper_method :current_user
  # def login
  #   @user = User.new
  #   # @user.role = params[:fake_role].blank? ? :client : params[:fake_role].to_sym
  # end
  # def current_user
  #   @user
  # end
end