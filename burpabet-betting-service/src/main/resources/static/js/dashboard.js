var AppDashboard = function (settings) {
    this.settings = settings;
    this.init();
};

AppDashboard.prototype = {
    init: function () {
        this.betSummaryContainer = this.getElement(this.settings.elements.betSummaryContainer);
        this.raceSummaryContainer = this.getElement(this.settings.elements.raceSummaryContainer);

        this.loadInitialBets();
        this.loadInitialRaces();

        this.addWebsocketListener();
    },

    loadInitialBets: function () {
        var _this = this;

        $.get(this.settings.endpoints.settledBets, function (data) {
            _this.createBetElements(data['_embedded']['betting:bet-list']);
        });
    },

    loadInitialRaces: function () {
        var _this = this;

        $.get(this.settings.endpoints.allRaces, function (data) {
            _this.createRaceElements(data['_embedded']['betting:race-list']);
        });
    },

    getElement: function (id) {
        return $('#' + id);
    },

    createBetElements: function (data) {
        var _this = this, report;

        // console.log(data);

        report = data.map(function (bet) {
            var colorClass = '';

            if (bet.race.outcome==='lose') {
                colorClass='table-danger';
            }
            // if (bet.payout.amount>50) {
            //     colorClass='table-success';
            // }

            return $('<tr>')
                .attr('class', colorClass)
                .append(
                    $('<td>')
                        .attr('scope', 'row')
                        .append($('<a>')
                            .attr('href', bet._links.self.href)
                            .text(bet.customerName))
                )
                .append(
                    $('<td>')
                        // .attr('scope', 'row')
                        .text(bet.race.track)
                )
                .append(
                    $('<td>')
                        .attr('id', "horse")
                        .text(bet.race.horse)
                )
                .append(
                    $('<td>')
                        .attr('id', "odds")
                        .text(bet.race.odds)
                )
                .append(
                    $('<td>')
                        .attr('id', "outcome")
                        .text(bet.race.outcome)
                )
                .append(
                    $('<td>')
                        .attr('id', "winnings")
                        .text(_this.formatMoney(bet.payout.amount, bet.payout.currency))
                )
        });

        this.betSummaryContainer.empty();
        this.betSummaryContainer.append(report);
    },

    createRaceElements: function (data) {
        var _this = this, report;

        // console.log(data);

        report = data.map(function (race) {
            var colorClass = '';

            if (race.outcome === 'lose') {
                colorClass = 'table-danger';
            }
            return $('<tr>')
                    .attr('class', colorClass)
                    .append(
                            $('<td>')
                                    .attr('scope', 'row')
                                    .append($('<a>')
                                        .attr('href', race._links.self.href)
                                        .text(race.track))
                    )
                    .append(
                            $('<td>')
                                    .attr('id', "horse")
                                    .text(race.horse)
                    )
                    .append(
                            $('<td>')
                                    .attr('id', "odds")
                                    .text(race.odds)
                    )
                    .append(
                            $('<td>')
                                    .attr('id', "outcome")
                                    .text(race.outcome)
                    )
                    .append(
                            $('<td>')
                                    .attr('id', "totalBets")
                                    .text(race.totalBets)
                    )
                    .append(
                            $('<td>')
                                    .attr('id', "totalWager")
                                    .text(_this.formatMoney(race.totalWager.amount, race.totalWager.currency))
                    )
                    .append(
                            $('<td>')
                                    .attr('id', "totalPayout")
                                    .text(_this.formatMoney(race.totalPayout.amount, race.totalPayout.currency))
                    )
        });

        this.raceSummaryContainer.empty();
        this.raceSummaryContainer.append(report);
    },

    addWebsocketListener: function () {
        var socket = new SockJS(this.settings.endpoints.socket),
                stompClient = Stomp.over(socket),
                _this = this;

        stompClient.connect({}, function (frame) {
            stompClient.subscribe(_this.settings.topics.betSummary, function (report) {
                var event = JSON.parse(report.body);
                // console.log("Stomp event: " + event);
                _this.handleBetSummaryUpdate(event);
            });
        });
    },

    handleBetSummaryUpdate: function (summary) {
        var _this = this;

        _this.loadInitialBets();
    },

    formatMoney: function (number, currency) {
        var formatter = new Intl.NumberFormat('en-US', {
            style: 'currency',
            currency: currency,
        });
        return formatter.format(number);
    }
};

document.addEventListener('DOMContentLoaded', function () {
    new AppDashboard({
        endpoints: {
            settledBets: '/api/bet/settled',
            allRaces: '/api/race',
            socket: '/burpabet-betting'
        },

        topics: {
            betSummary: '/topic/bet-summary'
        },

        elements: {
            betSummaryContainer: 'bet-summary-container',
            raceSummaryContainer: 'race-summary-container'
        }
    });
});

