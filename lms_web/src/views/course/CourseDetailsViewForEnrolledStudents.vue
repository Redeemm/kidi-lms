<template>
    <div class="container-fluid px-auto px-md-5">
        <div class="row">
            <div class="col-12 bg-light mt-3">
                <h2>{{name}}</h2>
            </div>
            <div class="col-md-9 mt-3">
                <nav class="ps-auto ps-md-0">
                    <div class="nav nav-tabs" id="nav-tab" role="tablist">
                        <button class="nav-link active" id="nav-home-tab" data-bs-toggle="tab" data-bs-target="#nav-home" type="button" role="tab" aria-controls="nav-home" aria-selected="true"><i class="fa-solid fa-chart-line"></i>Activity</button>
                        <button class="nav-link" id="nav-profile-tab" data-bs-toggle="tab" data-bs-target="#nav-profile" type="button" role="tab" aria-controls="nav-profile" aria-selected="false"><i class="fa-solid fa-scroll"></i>Announcements</button>
                        <button class="nav-link" id="nav-contact-tab" data-bs-toggle="tab" data-bs-target="#nav-contact" type="button" role="tab" aria-controls="nav-contact" aria-selected="false"><i class="fa-solid fa-circle-info"></i>Details</button>
                    </div>
                </nav>
                <div class="tab-content" id="nav-tabContent">
                    <div class="tab-pane fade show active mt-3" id="nav-home" role="tabpanel" aria-labelledby="nav-home-tab">
                        <EmptyState title="No avtivity found!" />
                    </div>
                    <div class="tab-pane fade" id="nav-profile" role="tabpanel" aria-labelledby="nav-profile-tab">
                        <EmptyState class="mt-3" title="No announcements found!" v-if="isAnnouncementsEmplty" />
                        <div class="announcements mt-3" v-else>
                            <Accordion
                                v-for="a in announcements" 
                                :key="a.id" 
                                :id="a.id" 
                                :title="a.title" 
                                :description="a.description"
                            />
                        </div>
                    </div>
                    <div class="tab-pane fade" id="nav-contact" role="tabpanel" aria-labelledby="nav-contact-tab">
                        <section class="mt-3">
                            <h2>Course Details</h2>
                            <hr>
                            <h5 class="text-gray">Course name: {{name}}</h5>
                            <h5 class="text-gray">Course code: {{code}}</h5>
                            <h5 class="text-gray">Lecturer: {{lecturer.name}}</h5>
                            <p class="text-gray mt-3">{{description}}</p>
                        </section>
                        <section class="mt-5">
                            <h2 class="text-danger">Unenrollment</h2>
                            <hr>
                            <p class="text-gray">If you are not interested in this course, you can just un-enroll to the course.</p>
                            <button class="btn btn-outline-danger" style="border-width: 2px;" @click="confirmUnenroll">Unenroll</button>
                        </section>
                    </div>
                </div>
            </div>
            <div class="col-md-3">
                <div class="marks mt-3">
                    <h4 class="text-center mt-3">Marks</h4>
                    <p class="text-gray text-center">{{marks}}</p>
                </div>
                <div>
                    <h4 class="text-center mt-3">Other courses</h4>
                    <p class="text-gray text-center mt-3" v-if="isEnrolledCoursesEmpty">No other courses!</p>
                    <div class="enrolled-courses mt-4" v-else>
                        <CourseListItem
                            v-for="course in enrolledCourses"
                            :key="course.course_id"
                            :id="course.course_id"
                            :name="course.name"
                            :code="course.course_code"
                            @click="initComponent(course.course_id)"
                        />
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script>
import EmptyState from '@/components/EmptyState.vue'
import CourseListItem from '@/components/CourseListItem.vue'
import Accordion from '@/components/Accordion.vue'

import userService from '@/services/UserServices.js'
import courseService from '@/services/CourseService.js'

import Swal from 'sweetalert2'

export default {
    name: 'CourseDetailsForEnrolledstudents',
    components: {
        EmptyState,
        CourseListItem,
        Accordion
    },
    props: {
        id: String
    },
    data(){
        return{
            code: "Loading...",
            name: "Loading...",
            description: "Loading...",
            lecturer: {
                name: "Loading..."
            },
            enrolledCourses: [],
            announcements: [],
            marks: "Loading..."
        }
    },
    methods: {
        initComponent(courseId){

            //course details
            courseService.getCourseDetails(
                courseId, userService.getToken()
            ).then(data => {
                this.code = data.course_code
                this.name = data.name
                this.description = data.description
                this.lecturer.name = data.lecturer.name
            }).catch(err => {
                console.log(err);
            })

            //announcements
            courseService.getAnnouncements(
                courseId, userService.getToken()
            ).then(data => {
                this.announcements = data
            }).catch(err => {
                console.log(err)
            })

            //enrolled courses
            userService.getEnrolledCourses(
                userService.getUserDetails().id, userService.getToken()
            ).then(data => {
                this.enrolledCourses = data.data.filter(item => {return item.course_id != courseId})
            }).catch(err => {
                console.log(err)
            })

            //display marks
            userService.getMarks(userService.getUserDetails().id, courseId).then(res => {
                if(res.data == ""){
                    this.marks = "Not asssigned yet!"
                }else{
                    this.marks = res.data
                }
            }).catch(err => {
                console.log(err)
            })
        },

        confirmUnenroll(){
            const swalWithBootstrapButtons = Swal.mixin({
                customClass: {
                    confirmButton: 'btn btn-danger me-2',
                    cancelButton: 'btn btn-outline-success'
                },
                buttonsStyling: false
            })

            swalWithBootstrapButtons.fire({
                title: 'Are you sure?',
                text: "You won't be able to revert this!",
                icon: 'warning',
                showCancelButton: true,
                confirmButtonText: 'Yes, unenroll!',
                cancelButtonText: 'No, cancel!',
                reverseButtons: false
            }).then((result) => {
                if (result.isConfirmed) {
                    userService.unenroll(
                        userService.getUserDetails().id,
                        this.id,
                        userService.getToken()
                    ).then(res => {
                        swalWithBootstrapButtons.fire(
                            'Unenrolled!',
                            'You have successfully unenrolled from this course.',
                            'success'
                        ).then(() => {
                            this.$router.go(0)
                        })
                    }).catch(err => {
                        console.log(err)
                    })
                }
            })
        }
    },
    computed: {
        isAnnouncementsEmplty(){
            return this.announcements.length == 0
        },
        isEnrolledCoursesEmpty(){
            return this.enrolledCourses.length == 0
        }
    },
    mounted(){
        if(!userService.isSigned){
            this.$router.push('/signin')
            return
        }
        this.initComponent(this.id)
    }
}
</script>

<style scoped>
    .nav-link{
        color: rgb(126, 126, 126);
    }
    .nav-link.active{
        color: #946B2D;
    }
    i{
        margin-right: 8px;
    }
</style>