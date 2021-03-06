package com.cuentasclaras.commands

import com.cuentasclaras.utils.Logger
import grails.converters.JSON
import grails.util.Holders
import grails.validation.Validateable

/**
 * Created by gmazzaglia on 3/7/17.
 */
@Validateable
class MovementListCommand {

    def grailsApplication = Holders.applicationContext.getBean("grailsApplication")

    static String PERIOD_TODAY = "today"
    static String PERIOD_THISWEEK = "thisweek"
    static String PERIOD_THISMONTH = "thismonth"
    static String PERIOD_LASTMONTH = "lastmonth"

    String search
    String filter_perdiod
    String dateIni
    String dateEnd
    List<Long> tags
    List<Long> types

    static constraints = {
        search(nullable: true, blank: true)
        filter_perdiod(nullable: true, blank: true, inList: [PERIOD_TODAY, PERIOD_THISWEEK, PERIOD_THISMONTH, PERIOD_LASTMONTH])
        dateIni(nullable: true, blank: true)
        dateEnd(nullable: true, blank: true)
        types(nullable: true, blank: true)
    }

    Map getPeriodToday() {
        Logger.trace([:], "Init getPeriodToday()");
        Date ini = new Date();
        use(groovy.time.TimeCategory) {
            ini += (grailsApplication.config.timeZone).hours
        }
        ini = ini.clearTime();
        Date end = ini;
        use(groovy.time.TimeCategory) {
            end = end + 23.hours + 59.minutes + 59.seconds
        }
        return [ini: ini, end: end];
    }

    Map getPeriodThisWeek() {
        Logger.trace([:], "Init getPeriodThisWeek()");
        Date ini = null;
        Date end = new Date();
        use(groovy.time.TimeCategory) {
            end += (grailsApplication.config.timeZone).hours
        }
        use(groovy.time.TimeCategory) {
            ini = end - 7.days
        }
        ini = ini.clearTime();
        return [ini: ini, end: end];
    }

    Map getPeriodThisMonth() {
        Logger.trace([:], "Init getPeriodThisMonth()");
        Date ini = null;
        Date end = null;
        Calendar c = Calendar.getInstance();
        c.set(Calendar.DAY_OF_MONTH, 1);
        ini = c.getTime();
        ini = ini.clearTime();
        use(groovy.time.TimeCategory) {
            end = ini + 1.month - 1.day + 23.hours + 59.minutes + 59.seconds
        }
        return [ini: ini, end: end];
    }

    Map getPeriodLastMonth() {
        Logger.trace([:], "Init getPeriodLastMonth()");
        Date ini = null;
        Date end = null;
        Calendar c = Calendar.getInstance();
        c.set(Calendar.DAY_OF_MONTH, 1);
        ini = c.getTime();
        ini = ini.clearTime();
        use(groovy.time.TimeCategory) {
            ini = ini - 1.month
        }
        use(groovy.time.TimeCategory) {
            end = ini + 1.month - 1.day + 23.hours + 59.minutes + 59.seconds
        }
        return [ini: ini, end: end];
    }

    Map<String, Date> getPeriodCustom() {
        Logger.trace([:], "Init getPeriodCustom()");
        Date ini = null;
        Date end = null;
        try {
            if (this.dateIni) {
                ini = new Date().parse("dd/MM/yyyy", this.dateIni)
            } else {
                Date tmp = new Date();
                use(groovy.time.TimeCategory) {
                    tmp += (grailsApplication.config.timeZone).hours
                }
                ini = new Date().parse("dd/MM/yyyy", tmp.format("dd/MM/yyyy"))
            }
            ini = ini.clearTime();
            if (this.dateEnd) {
                end = new Date().parse("dd/MM/yyyy HH:mm:ss", this.dateEnd + " 23:59:59")
            } else {
                end = new Date();
                use(groovy.time.TimeCategory) {
                    end += (grailsApplication.config.timeZone).hours
                }
                end = end.clearTime();
                use(groovy.time.TimeCategory) {
                    end = end + 23.hours + 59.minutes + 59.seconds
                }
            }
        } catch (Exception) {
            if (this.dateIni) {
                ini = new Date().parse("yyyy-MM-dd", this.dateIni)
            } else {
                Date tmp = new Date();
                use(groovy.time.TimeCategory) {
                    tmp += (grailsApplication.config.timeZone).hours
                }
                ini = new Date().parse("yyyy-MM-dd", tmp.format("yyyy-MM-dd"))
            }
            ini = ini.clearTime();
            if (this.dateEnd) {
                end = new Date().parse("yyyy-MM-dd HH:mm:ss", this.dateEnd + " 23:59:59")
            } else {
                end = new Date();
                use(groovy.time.TimeCategory) {
                    end += (grailsApplication.config.timeZone).hours
                }
                end = end.clearTime();
                use(groovy.time.TimeCategory) {
                    end = end + 23.hours + 59.minutes + 59.seconds
                }
            }
        }
        return [ini: ini, end: end];
    }

    boolean isFilterPeriodToday() {
        return this.filter_perdiod == PERIOD_TODAY;
    }

    boolean isFilterPeriodThisWeek() {
        return this.filter_perdiod == PERIOD_THISWEEK;
    }

    boolean isFilterPeriodThisMonth() {
        return this.filter_perdiod == PERIOD_THISMONTH;
    }

    boolean isFilterPeriodLastMonth() {
        return this.filter_perdiod == PERIOD_LASTMONTH;
    }

    Map getFilterPeriod() {
        Map result = [:];
        if (this.dateIni || this.dateEnd) {
            result = this.getPeriodCustom();
        } else if (this.isFilterPeriodToday()) {
            result = this.getPeriodToday();
        } else if (this.isFilterPeriodThisWeek()) {
            result = this.getPeriodThisWeek();
        } else if (this.isFilterPeriodThisMonth()) {
            result = this.getPeriodThisMonth();
        } else if (this.isFilterPeriodLastMonth()) {
            result = this.getPeriodLastMonth();
        }
        if (result.size() == 0) {
            result = this.getPeriodThisMonth();
        }
        Logger.trace([
                date_ini: result.ini.format("yyyy-MM-dd HH:mm:ss"),
                date_end: result.end.format("yyyy-MM-dd HH:mm:ss")
        ], "getFilterPeriod() result");
        return result;
    }

    Map getTheErrors() {
        Map result = [:];
        this.getErrors().getFieldErrors().each { e ->
            String field = e.field.capitalize()
            result << ["${field}": "invalid param"];
        }
        return result
    }

    Map getValues() {
        return [
                search        : search,
                filter_perdiod: filter_perdiod,
                dateIni       : dateIni,
                dateEnd       : dateEnd,
                tags          : tags,
                types         : types,
        ];
    }

    @Override
    String toString() {
        return this.getValues() as JSON;
    }

}
