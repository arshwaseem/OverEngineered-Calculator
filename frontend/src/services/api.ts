import axios, {AxiosError, AxiosInstance, AxiosRequestConfig, AxiosResponse, InternalAxiosRequestConfig} from 'axios';

const api:AxiosInstance = axios.create({
    baseURL:"/api",
    withCredentials:true,
    headers:{
        Accept : 'application/json',
    },
})

api.interceptors.request.use(
    (config : InternalAxiosRequestConfig) => {
        return config;
    }, (error:AxiosError) => {
        return Promise.reject(error);
    }
)

api.interceptors.response.use(
    (response : AxiosResponse) => {
        return response;
    }, async(error : AxiosError) => {
        const originalRequest = error.config as InternalAxiosRequestConfig & {_retry ?: boolean}

        if(error.response === 401 && !originalRequest._retry){

            originalRequest._retry = true;

            try{
                await api.post("/auth/refresh");
                return api(originalRequest);
            } catch (refreshError) {
                window.location.href = "/login";
                return Promise.reject(refreshError);
            }
        }
        return Promise.reject(error);
    }


const apiService = {
}
)